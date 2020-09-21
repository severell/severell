package com.severell.core;

import com.severell.core.commands.Migrate;
import com.severell.core.config.Config;
import com.severell.core.database.grammar.PostgresGrammar;
import com.severell.core.database.migrations.Connection;
import com.severell.core.database.migrations.PostgresConnection;
import com.severell.core.database.migrations.PostgresQueryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class MigrateTest {

    private static ByteArrayOutputStream outContent;
    private static ByteArrayOutputStream errContent;
    private static final PrintStream originalOut = System.out;
    private static final PrintStream originalErr = System.err;
    private static Connection connection;

    @BeforeEach
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @BeforeAll
    public static void setup() throws Exception {
        if(!Config.isLoaded()) {
            Config.setDir("src/test/resources");
            Config.loadConfig();
        }

        connection = mock(PostgresConnection.class);
        PostgresQueryBuilder builder = new PostgresQueryBuilder();
        given(connection.getDefaultGrammar()).willReturn(new PostgresGrammar());
        given(connection.getDefaultQueryBuilder()).willReturn(builder);

        //Mocking Has Table Call
        List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        list.add(new HashMap<>());
        given(connection.select("select * from information_schema.tables where table_schema = ? and table_name = ? and table_type = 'BASE TABLE'", "public", "migrations")).willReturn(list);

    }

    @Test
    public void runUpTest() throws Exception {
        Migrate migrate = new Migrate(connection);
        Migrate spyMigrate = spy(migrate);
        spyMigrate.runUp(null);

        assertTrue(outContent.toString().contains("Migrated - TestMigration"));
    }

    @Test
    public void runUpTestWithNothingToMigration() throws Exception {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        HashMap<String, Object> m = new HashMap<>();
        m.put("migration", "TestMigration");
        list.add(m);
        given(connection.select("select * from migrations order by batch asc, migration asc")).willReturn(list);
        Migrate migrate = new Migrate(connection);
        Migrate spyMigrate = spy(migrate);

        spyMigrate.runUp(null);

        assertTrue(outContent.toString().contains("Nothing to Migrate"));
    }
}
