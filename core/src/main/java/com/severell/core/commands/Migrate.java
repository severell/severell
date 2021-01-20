package com.severell.core.commands;

import com.severell.core.config.Config;
import com.severell.core.database.Connection;
import com.severell.core.database.migrations.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Migrate {

    private static Connection connection;
    private static DatabaseMigrationRepository repository;
    private static URLClassLoader loader;
    private String migrationPath;

    public Migrate(Connection connection) {
        this.connection = connection;
        this.migrationPath = Config.get("MIGRATION_PATH", "src/db/migrations");
    }

    public void runUp() throws Exception {
        List<Class> pendingList = getPendingMigrations(prepareMigrations());

        if(pendingList.size() == 0) {
            return;
        }

        long batch = repository.getNextBatchNumber();

        for(Class cl : pendingList) {
            Method method = cl.getDeclaredMethod("up");
            LocalDateTime start = LocalDateTime.now();
            CommandLogger.printlnRed(String.format("Migrating - %s", cl.getSimpleName()));

            try {
                method.invoke(null);
                repository.log(cl.getSimpleName(), batch);
                LocalDateTime end = LocalDateTime.now();
                long diff = ChronoUnit.MILLIS.between(start, end);
                CommandLogger.printlnGreen(String.format("Migrated - %s (%d ms)", cl.getSimpleName(), diff));
                CommandLogger.println("");
            }catch (InvocationTargetException e) {
                MigrationException me = (MigrationException) e.getCause();
                CommandLogger.printlnRed(String.format("Failed to Migrate - %s", cl.getSimpleName()));
                CommandLogger.printlnRed(String.format("Reason - %s", me.getMessage()));
                CommandLogger.println("");
            }

        }
    }

    private ArrayList<Class> prepareMigrations() throws Exception {
        loader = setupClassLoader();
        CommandLogger.printlnGreen("Starting Migrator...");
        createConnection();
        Builder b = new PostgresBuilder(connection);
        Schema.setBuilder(b);
        prepareDatabase();

        return getMigrations("migrations");
    }

    public void reset() throws Exception {
        List<Class> resetList = getMigrationsToReset(prepareMigrations());

        if(resetList.size() == 0){
            CommandLogger.printlnRed(String.format("Nothing to reset"));
            return;
        }

        runDown(resetList);
    }

    public void rollback() throws Exception {
        List<Class> resetList = getMigrationsToRollback(prepareMigrations());

        if(resetList.size() == 0){
            CommandLogger.printlnRed(String.format("Nothing to reset"));
            return;
        }

        runDown(resetList);
    }

    private URLClassLoader setupClassLoader() throws Exception {
        Path p = Paths.get("target/classes");
        File f = p.toFile();
        URL[] urls = new URL[]{f.toURI().toURL()};
        URLClassLoader loader = URLClassLoader.newInstance(urls,Migrate.class.getClassLoader());
        return loader;
    }

    private void runDown(List<Class> resetList) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for(Class cl : resetList) {
            try {
                Method method = cl.getDeclaredMethod("down");
                LocalDateTime start = LocalDateTime.now();
                CommandLogger.printlnRed(String.format("Rolling Back - %s", cl.getSimpleName()));
                method.invoke(null);
                repository.delete(cl.getSimpleName());
                LocalDateTime end = LocalDateTime.now();
                long diff = ChronoUnit.MILLIS.between(start, end);
                CommandLogger.printlnGreen(String.format("Rolled Back - %s (%d ms)", cl.getSimpleName(), diff));
                CommandLogger.println("");
            }catch (InvocationTargetException e) {
                MigrationException me = (MigrationException) e.getCause();
                CommandLogger.printlnRed(String.format("Failed to Reset - %s", cl.getSimpleName()));
                CommandLogger.printlnRed(String.format("Reason - %s", me.getMessage()));
                CommandLogger.println("");
            }

        }
    }

    private List<Class> getPendingMigrations(Iterable<Class> list) {
        List<Class> pendingMigrations = new ArrayList<Class>();
        List<HashMap<String, Object>> ranMigrations = repository.getRan();
        for(Class cl : list) {
            boolean found = false;
            for(HashMap<String, Object> mig : ranMigrations) {
                if(mig.get("migration").equals(cl.getSimpleName())) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                pendingMigrations.add(cl);
            }
        }

        return pendingMigrations;

    }

    private List<Class> getMigrationsToReset(Iterable<Class> list) {
        List<Class> resetMigrations = new ArrayList<Class>();
        List<HashMap<String, Object>> ranMigrations = repository.getRan();
        for(HashMap<String, Object> mig : ranMigrations) {
            for(Class cl : list) {
                if(mig.get("migration").equals(cl.getSimpleName())) {
                    resetMigrations.add(cl);
                }
            }
        }

        return resetMigrations;
    }

    private List<Class> getMigrationsToRollback(Iterable<Class> list) {
        List<Class> resetMigrations = new ArrayList<Class>();
        List<HashMap<String, Object>> ranMigrations = repository.getLast();
        for(HashMap<String, Object> mig : ranMigrations) {
            for(Class cl : list) {
                if(mig.get("migration").equals(cl.getSimpleName())) {
                    resetMigrations.add(cl);
                }
            }
        }

        return resetMigrations;
    }

    private void prepareDatabase() throws SQLException {
        repository = new DatabaseMigrationRepository(connection);
        if( !repository.repositoryExists()) {
            try {
                repository.createRepository();
            }catch (MigrationException e ) {
                e.printStackTrace();
            }
        }
    }

    private void createConnection() {

    }

    private ArrayList<Class> getMigrations(String packageName) throws ClassNotFoundException, IOException, URISyntaxException {
        ArrayList<Class> classList = new ArrayList<>();

        Path p = Paths.get(migrationPath);
        File f = p.toFile();
        String[] migrationFiles = f.getAbsoluteFile().list();


        if(migrationFiles != null) {
            for (String file : migrationFiles) {
                classList.add(loader.loadClass("migrations" + "." + file.substring(0, file.length() - 5)));
            }

            Collections.sort(classList, new Comparator<Class>() {
                @Override
                public int compare(Class o1, Class o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }

        return classList;
    }

}
