package com.severell.core.database.migrations;

import com.severell.core.database.Connection;
import com.severell.core.database.grammar.PostgresGrammar;
import com.severell.core.database.migrations.Blueprint;
import org.codehaus.plexus.util.cli.Arg;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MigrationIntegrationTest {

    @Test
    public void testMigrationAddsPrimaryKeyPostgres() throws MigrationException {
        Blueprint table = new Blueprint("users");
        table.create();

        table.id();
        table.primary("id");
        table.bigInteger("post_id");
        table.foreign("post_id").references("id").on("post");
        table.string("name");
        table.timestamp("created");
        table.timestamps();
        table.binary("bin");
        table.bool("isUser");
        table.integer("age");
        table.text("html");
        table.decimal("acres", 5, 3);
        table.dbl("dbl");
        table.date("somedate").nullable();

        PostgresGrammar g = new PostgresGrammar();
        Connection connection = mock(Connection.class);

        table.build(connection, g);

        ArgumentCaptor<String> statements = ArgumentCaptor.forClass(String.class);
        verify(connection, times(3)).statement(statements.capture());

        assertEquals("create table users (id bigserial not null, post_id bigint not null, name varchar(255) not null, created timestamp(0) without time zone not null, created_at timestamp(0) without time zone null, updated_at timestamp(0) without time zone null, bin bytea not null, isUser boolean not null, age integer not null, html text not null, acres decimal(5,3) not null, dbl double precision not null, somedate date null)", statements.getAllValues().get(0));
        assertEquals("alter table users add primary key (id)", statements.getAllValues().get(1));
        assertEquals("alter table users add constraint users_post_id_foreign foreign key (post_id) references post (id)", statements.getAllValues().get(2));
//        System.out.println(statements);
    }

    @Test
    public void testMigrationDropPostgres() throws MigrationException {
        Blueprint table = new Blueprint("users");
        table.drop();


        PostgresGrammar g = new PostgresGrammar();
        Connection connection = mock(Connection.class);

        table.build(connection, g);

        ArgumentCaptor<String> statements = ArgumentCaptor.forClass(String.class);
        verify(connection, times(1)).statement(statements.capture());
        assertEquals("drop table users", statements.getValue());

//        System.out.println(statements);
    }

    @Test
    public void testMigrationAddPostgres() throws MigrationException {
        Blueprint table = new Blueprint("users");

        table.string("addedColumn");

        PostgresGrammar g = new PostgresGrammar();
        Connection connection = mock(Connection.class);

        table.build(connection, g);

        ArgumentCaptor<String> statements = ArgumentCaptor.forClass(String.class);
        verify(connection, times(1)).statement(statements.capture());
        assertEquals("alter table users add column addedColumn varchar(255) not null", statements.getValue());

//        System.out.println(statements);
    }
}
