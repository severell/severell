package com.severell.core.database.migrations;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SchemaTest {

    @Test
    public void testSchemaCreate() throws MigrationException {
        Builder builder = mock(Builder.class);
        AtomicReference<Blueprint> actualTable = new AtomicReference<>();

        Schema.setBuilder(builder);
        Schema.create("users",(Blueprint table) -> {
            actualTable.set(table);
            assertEquals("users", table.getTableName());
        });

        verify(builder).build(actualTable.get());
    }

    @Test
    public void testSchemaUpdate() throws MigrationException {
        Builder builder = mock(Builder.class);
        AtomicReference<Blueprint> actualTable = new AtomicReference<>();

        Schema.setBuilder(builder);
        Schema.table("users",(Blueprint table) -> {
            actualTable.set(table);
            assertEquals("users", table.getTableName());
        });

        verify(builder).build(actualTable.get());
    }

    @Test
    public void testSchemaDrop() throws MigrationException {
        Builder builder = mock(Builder.class);
        Schema.setBuilder(builder);
        Schema.drop("users");

        ArgumentCaptor<Blueprint> tableCapt = ArgumentCaptor.forClass(Blueprint.class);
        verify(builder).build(tableCapt.capture());

        assertEquals("users", tableCapt.getValue().getTableName());
    }
}
