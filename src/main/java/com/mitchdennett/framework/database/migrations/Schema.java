package com.mitchdennett.framework.database.migrations;

import java.util.function.Consumer;

public class Schema {

    private static Builder builder;

    public static void setBuilder(Builder builder) {
        Schema.builder = builder;
    }

    public static Builder getBuilder() {
        return builder;
    }

    public static void create(String tableName, Consumer<Blueprint> function) {
        Blueprint table = new Blueprint(tableName);
        table.create();
        function.accept(table);
        builder.build(table);
    }

    public static void drop(String tableName) {
        Blueprint table = new Blueprint(tableName);
        table.drop();
        builder.build(table);
    }
}
