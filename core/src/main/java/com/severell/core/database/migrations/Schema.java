package com.severell.core.database.migrations;

import java.util.function.Consumer;

public class Schema {

    private static Builder builder;

    public static void setBuilder(Builder builder) {
        Schema.builder = builder;
    }

    public static Builder getBuilder() {
        return builder;
    }

    public static void create(String tableName, Consumer<Blueprint> function) throws MigrationException {
        Blueprint table = new Blueprint(tableName);
        table.create();
        function.accept(table);
        builder.build(table);
    }

    public static void drop(String tableName) throws MigrationException {
        Blueprint table = new Blueprint(tableName);
        table.drop();
        builder.build(table);
    }

    public static void table(String tableName, Consumer<Blueprint> function) throws MigrationException {
        Blueprint table = new Blueprint(tableName);
        function.accept(table);
        builder.build(table);
    }
}
