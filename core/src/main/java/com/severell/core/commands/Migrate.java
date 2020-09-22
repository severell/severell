package com.severell.core.commands;

import com.severell.core.config.Config;
import com.severell.core.database.migrations.*;
import org.apache.commons.dbcp2.BasicDataSource;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Migrate {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private static Connection connection;
    private static DatabaseMigrationRepository repository;
    private static URLClassLoader loader;
    private String migrationPath;

    public Migrate(Connection connection) {
        this.connection = connection;
        this.migrationPath = Config.get("MIGRATION_PATH", "src/db/migrations");
    }

    public void runUp(String[] args) throws Exception {
        List<Class> pendingList = getPendingMigrations(prepareMigrations());

        if(pendingList.size() == 0) {
            System.out.println(String.format("%s Nothing to Migrate %s", ANSI_RED, ANSI_RESET));
            return;
        }

        long batch = repository.getNextBatchNumber();

        for(Class cl : pendingList) {
            Method method = cl.getDeclaredMethod("up");
            LocalDateTime start = LocalDateTime.now();
            System.out.println(String.format("%s Migrating - %s %s", ANSI_RED, cl.getSimpleName(), ANSI_RESET));
            try {
                method.invoke(null);
                repository.log(cl.getSimpleName(), batch);
                LocalDateTime end = LocalDateTime.now();
                long diff = ChronoUnit.MILLIS.between(start, end);
                System.out.println(String.format("%s Migrated - %s (%d ms) %s", ANSI_GREEN, cl.getSimpleName(), diff, ANSI_RESET));
                System.out.println("");
            }catch (InvocationTargetException e) {
                MigrationException me = (MigrationException) e.getCause();
                System.out.println(String.format("%s Failed to Migrate - %s  %s", ANSI_RED, cl.getSimpleName(), ANSI_RESET));
                System.out.println(String.format("%s Reason - %s  %s", ANSI_RED, me.getMessage(), ANSI_RESET));
                System.out.println("");
            }

        }
    }

    private ArrayList<Class> prepareMigrations() throws Exception {
        loader = setupClassLoader();
        System.out.println(String.format("%s Starting Migrator... %s", ANSI_GREEN, ANSI_RESET));
        createConnection();
        Builder b = new PostgresBuilder(connection);
        Schema.setBuilder(b);
        prepareDatabase();

        return getMigrations("migrations");
    }

    public void reset(String[] args) throws Exception {
        List<Class> resetList = getMigrationsToReset(prepareMigrations());

        if(resetList.size() == 0){
            System.out.println(String.format("%s Nothing to reset %s", ANSI_RED, ANSI_RESET));
            return;
        }

        runDown(resetList);
    }

    public void rollback(String[] args) throws Exception {
        List<Class> resetList = getMigrationsToRollback(prepareMigrations());

        if(resetList.size() == 0){
            System.out.println(String.format("%s Nothing to reset %s", ANSI_RED, ANSI_RESET));
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
                System.out.println(String.format("%s Rolling Back - %s %s", ANSI_RED, cl.getSimpleName(), ANSI_RESET));
                method.invoke(null);
                repository.delete(cl.getSimpleName());
                LocalDateTime end = LocalDateTime.now();
                long diff = ChronoUnit.MILLIS.between(start, end);
                System.out.println(String.format("%s Rolled Back - %s (%d ms) %s", ANSI_GREEN, cl.getSimpleName(), diff, ANSI_RESET));
                System.out.println("");
            }catch (InvocationTargetException e) {
                MigrationException me = (MigrationException) e.getCause();
                System.out.println(String.format("%s Failed to Reset - %s  %s", ANSI_RED, cl.getSimpleName(), ANSI_RESET));
                System.out.println(String.format("%s Reason - %s  %s", ANSI_RED, me.getMessage(), ANSI_RESET));
                System.out.println("");
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
        }

        return classList;
    }

}
