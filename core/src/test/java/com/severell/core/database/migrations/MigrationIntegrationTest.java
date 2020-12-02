package com.severell.core.database.migrations;

import com.severell.core.database.Connection;
import com.severell.core.database.grammar.MySQLGrammer;
import com.severell.core.database.grammar.PostgresGrammar;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
        table.increments("incrementingInt");
        table.smallIncrements("smallIncrementing");
        table.smallInteger("smallInteger");
        table.bigIncrements("bigIncrements");

        PostgresGrammar g = new PostgresGrammar();
        Connection connection = mock(Connection.class);

        table.build(connection, g);

        ArgumentCaptor<String> statements = ArgumentCaptor.forClass(String.class);
        verify(connection, times(3)).statement(statements.capture());

        assertEquals("create table users (id bigserial not null, post_id bigint not null, name varchar(255) not null, created timestamp(0) without time zone not null, created_at timestamp(0) without time zone null, updated_at timestamp(0) without time zone null, bin bytea not null, isUser boolean not null, age integer not null, html text not null, acres decimal(5,3) not null, dbl double precision not null, somedate date null, incrementingInt serial not null, smallIncrementing smallserial not null, smallInteger smallint not null, bigIncrements bigserial not null)", statements.getAllValues().get(0));
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

    @Test
    public void testMigrationAddsPrimaryKeyMySQL() throws MigrationException {
        Blueprint table = new Blueprint("users");
        table.create();

        table.id();
        table.primary(true, "id");
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
        table.increments("incrementingInt");
        table.smallIncrements("smallIncrementing");
        table.smallInteger("smallInteger");
        table.bigIncrements("bigIncrements");

        MySQLGrammer g = new MySQLGrammer();
        Connection connection = mock(Connection.class);

        table.build(connection, g);

        ArgumentCaptor<String> statements = ArgumentCaptor.forClass(String.class);
        verify(connection, times(3)).statement(statements.capture());

        assertEquals("create table users (id bigint not null auto_increment, post_id bigint not null , name varchar(255) character set utf8mb4 not null , created timestamp(0) not null , created_at timestamp(0) null , updated_at timestamp(0) null , bin blob not null , isUser boolean not null , age integer not null , html text not null , acres decimal(5,3) not null , dbl double not null , somedate date null , incrementingInt int not null auto_increment, smallIncrementing smallint not null auto_increment, smallInteger smallint not null , bigIncrements bigint not null auto_increment)", statements.getAllValues().get(0));
        assertEquals("alter table users add primary key (id)", statements.getAllValues().get(1));
        assertEquals("alter table users add constraint users_post_id_foreign foreign key (post_id) references post (id)", statements.getAllValues().get(2));
    }

    @Test
    public void testMigrationDropMySQL() throws MigrationException {
        Blueprint table = new Blueprint("users");
        table.drop();


        MySQLGrammer g = new MySQLGrammer();
        Connection connection = mock(Connection.class);

        table.build(connection, g);

        ArgumentCaptor<String> statements = ArgumentCaptor.forClass(String.class);
        verify(connection, times(1)).statement(statements.capture());
        assertEquals("drop table if exists users", statements.getValue());

    }

    @Test
    public void testMigrationAddMySQL() throws MigrationException {
        Blueprint table = new Blueprint("users");

        table.string("addedColumn");

        MySQLGrammer g = new MySQLGrammer();
        Connection connection = mock(Connection.class);

        table.build(connection, g);

        ArgumentCaptor<String> statements = ArgumentCaptor.forClass(String.class);
        verify(connection, times(1)).statement(statements.capture());
        assertEquals("alter table users add column addedColumn varchar(255) character set utf8mb4 not null ", statements.getValue());

    }
}
