package com.severell.core.commands;

import com.severell.core.config.Config;
import com.severell.core.database.grammar.PostgresGrammar;
import com.severell.core.database.Connection;
import com.severell.core.database.PostgresConnection;
import com.severell.core.database.migrations.PostgresQueryBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

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

    /*
     * SETUP
     */

    @BeforeEach
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        connection = mock(PostgresConnection.class);
        PostgresQueryBuilder builder = new PostgresQueryBuilder();
        given(connection.getDefaultGrammar()).willReturn(new PostgresGrammar());
        given(connection.getDefaultQueryBuilder()).willReturn(builder);

        //Mocking Has Table Call
        List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        list.add(new HashMap<>());
        given(connection.select("select * from information_schema.tables where table_schema = ? and table_name = ? and table_type = 'BASE TABLE'", "public", "migrations")).willReturn(list);

    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @AfterAll
    public static void tearDown() {
        Config.unload();
    }

    @BeforeAll
    public static void setup() throws Exception {
        if(!Config.isLoaded()) {
            Config.setDir("src/test/resources");
            Config.loadConfig();
        }
    }

    @NotNull
    protected ArrayList<HashMap<String, Object>> getRan() {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        HashMap<String, Object> m = new HashMap<>();
        m.put("migration", "TestMigration");
        HashMap<String, Object> other = new HashMap<>();
        other.put("migration", "ErrorMigration");
        list.add(m);
        list.add(other);
        return list;
    }

    /*
     * TESTS
     */

    @Test
    public void runUpTestThroughCommand() throws Exception {
        MigrateCommand command = new MigrateCommand();
        command.setConnection(connection);
        command.run(new String[]{"args=", "flags="});

        assertTrue(outContent.toString().contains("Migrated - TestMigration"));
        assertTrue(outContent.toString().contains("Failed to Migrate - ErrorMigration"));
    }

    @Test
    public void runUpTest() throws Exception {
        Migrate migrate = new Migrate(connection);
        Migrate spyMigrate = spy(migrate);
        spyMigrate.runUp(null);

        assertTrue(outContent.toString().contains("Migrated - TestMigration"));
        assertTrue(outContent.toString().contains("Failed to Migrate - ErrorMigration"));
    }

    @Test
    public void runUpTestWithNothingToMigration() throws Exception {
        ArrayList<HashMap<String, Object>> list = getRan();
        given(connection.select("select * from migrations order by batch asc, migration asc")).willReturn(list);
        Migrate migrate = new Migrate(connection);
        migrate.runUp(null);

        assertTrue(outContent.toString().contains("Nothing to Migrate"), String.format("Got %s", outContent.toString()));
    }

    @Test
    public void runDownTest() throws Exception {
        ArrayList<HashMap<String, Object>> list = getRan();
        given(connection.select("select * from migrations order by batch asc, migration asc")).willReturn(list);


        Migrate migrate = new Migrate(connection);
        migrate.reset(null);

        assertTrue(outContent.toString().contains("Rolling Back - TestMigration"), String.format("Got %s", outContent.toString()));
        assertTrue(outContent.toString().contains("Failed to Reset - ErrorMigration"), String.format("Got %s", outContent.toString()));
    }

    @Test
    public void runDownTestWithNothingToReset() throws Exception {
        Migrate migrate = new Migrate(connection);
        migrate.reset(null);

        assertTrue(outContent.toString().contains("Nothing to reset"), String.format("Got %s", outContent.toString()));
    }

    @Test
    public void runRollbackTestThroughCommand() throws Exception {
        MigrateRollbackCommand command = new MigrateRollbackCommand();

        ArrayList<HashMap<String, Object>> list = getRan();
        //Mocking getLast()
        given(connection.select("select * from migrations where batch = 0 order by batch desc, migration desc limit 1")).willReturn(list);

        command.setConnection(connection);
        command.run(new String[]{"args=", "flags="});

        assertTrue(outContent.toString().contains("Rolling Back - TestMigration"), String.format("Got %s", outContent.toString()));
        assertTrue(outContent.toString().contains("Failed to Reset - ErrorMigration"), String.format("Got %s", outContent.toString()));
    }
    @Test
    public void runRollbackTest() throws Exception {
        ArrayList<HashMap<String, Object>> list = getRan();
        //Mocking getLast()
        given(connection.select("select * from migrations where batch = 0 order by batch desc, migration desc limit 1")).willReturn(list);


        Migrate migrate = new Migrate(connection);
        migrate.rollback(null);

        assertTrue(outContent.toString().contains("Rolling Back - TestMigration"), String.format("Got %s", outContent.toString()));
        assertTrue(outContent.toString().contains("Failed to Reset - ErrorMigration"), String.format("Got %s", outContent.toString()));
    }

    @Test
    public void runDownTestWithNothingToRollback() throws Exception {
        Migrate migrate = new Migrate(connection);
        migrate.rollback(null);

        assertTrue(outContent.toString().contains("Nothing to reset"), String.format("Got %s", outContent.toString()));
    }

}
