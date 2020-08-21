package com.mitchdennett.framework.commands;

import com.mitchdennett.framework.config.Config;
import com.mitchdennett.framework.database.migrations.*;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Enumeration;
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

    public static void runUp(String[] args) throws URISyntaxException, IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, SQLException {
        System.out.println(String.format("%s Starting Migrator... %s", ANSI_GREEN, ANSI_RESET));

        createConnection();
        Builder b = new PostgresBuilder(connection);
        Schema.setBuilder(b);
        prepareDatabase();

        Iterable<Class> list = getMigrations("migrations");
        List<Class> pendingList = getPendingMigrations(list);

        if(pendingList.size() == 0) {
            System.out.println(String.format("%s Nothing to Migrate %s", ANSI_RED, ANSI_RESET));
            return;
        }

        long batch = repository.getNextBatchNumber();

        for(Class cl : pendingList) {
            Method method = cl.getDeclaredMethod("up");
            LocalDateTime start = LocalDateTime.now();
            System.out.println(String.format("%s Migrating - %s %s", ANSI_RED, cl.getSimpleName(), ANSI_RESET));
            method.invoke(null);
            repository.log(cl.getSimpleName(), batch);
            LocalDateTime end = LocalDateTime.now();
            long diff = ChronoUnit.MILLIS.between(start, end);
            System.out.println(String.format("%s Migrated - %s (%d ms) %s", ANSI_GREEN, cl.getSimpleName(), diff, ANSI_RESET));
            System.out.println("");
        }
    }

    public static void reset(String[] args) throws URISyntaxException, IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, SQLException {
        System.out.println(String.format("%s Starting Migrator... %s", ANSI_GREEN, ANSI_RESET));
        createConnection();
        Builder b = new PostgresBuilder(connection);
        Schema.setBuilder(b);
        prepareDatabase();
        Iterable<Class> list = getMigrations("migrations");
        List<Class> resetList = getMigrationsToReset(list);

        if(resetList.size() == 0){
            System.out.println(String.format("%s Nothing to reset %s", ANSI_RED, ANSI_RESET));
            return;
        }

        runDown(resetList);
    }

    public static void rollback(String[] args) throws URISyntaxException, IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, SQLException {
        createConnection();
        Builder b = new PostgresBuilder(connection);
        Schema.setBuilder(b);
        prepareDatabase();
        Iterable<Class> list = getMigrations("migrations");
        List<Class> resetList = getMigrationsToRollback(list);

        runDown(resetList);
    }

    private static void runDown(List<Class> resetList) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for(Class cl : resetList) {
            Method method = cl.getDeclaredMethod("down");
            LocalDateTime start = LocalDateTime.now();
            System.out.println(String.format("%s Rolling Back - %s %s", ANSI_RED, cl.getSimpleName(), ANSI_RESET));
            method.invoke(null);
            repository.delete(cl.getSimpleName());
            LocalDateTime end = LocalDateTime.now();
            long diff = ChronoUnit.MILLIS.between(start, end);
            System.out.println(String.format("%s Rolled Back - %s (%d ms) %s", ANSI_GREEN, cl.getSimpleName(), diff, ANSI_RESET));
            System.out.println("");

        }
    }

    private static List<Class> getPendingMigrations(Iterable<Class> list) {
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

    private static List<Class> getMigrationsToReset(Iterable<Class> list) {
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

    private static List<Class> getMigrationsToRollback(Iterable<Class> list) {
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

    private static void prepareDatabase() throws SQLException {
        repository = new DatabaseMigrationRepository(connection);
        if( !repository.repositoryExists()) {
            repository.createRepository();
        }
    }

    private static void createConnection() {
        BasicDataSource connectionPool = new BasicDataSource();
        connectionPool.setUsername(Config.get("DB_USERNAME"));
        connectionPool.setPassword(Config.get("DB_PASSWORD"));
        connectionPool.setDriverClassName(Config.get("DB_DRIVER"));
        connectionPool.setUrl(Config.get("DB_CONNSTRING"));
        connectionPool.setInitialSize(1);
        connectionPool.setMinIdle(1);
        connectionPool.setMaxIdle(1);
        connection = new PostgresConnection();
        connection.setDataSource(connectionPool);
    }

    private static Iterable<Class> getMigrations(String packageName) throws ClassNotFoundException, IOException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements())
        {
            URL resource = resources.nextElement();
            URI uri = new URI(resource.toString());
            dirs.add(new File(uri.getPath()));
        }
        List<Class> classes = new ArrayList<Class>();
        for (File directory : dirs)
        {
            classes.addAll(findClasses(directory, packageName));
        }

        return classes;
    }

    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException
    {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists())
        {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            }
            else if (file.getName().endsWith(".class"))
            {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }



}
