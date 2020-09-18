package com.severell.core.database.migrations;

import com.severell.core.database.grammar.PostgresGrammar;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MigrationIntegrationTest {

    @Test
    public void testMigrationAddsPrimaryKeyPostgres() {
        Blueprint table = new Blueprint("users");
        table.create();

        table.id();
        table.primary("id");
        table.bigInteger("post_id");
        table.foreign("post_id").references("id").on("post");

        PostgresGrammar g = new PostgresGrammar();
        ArrayList<String> statements = table.compileCommands(g);

        assertEquals("create table users (id bigserial not null, post_id bigint not null)", statements.get(0));
        assertEquals("alter table users add primary key (id)", statements.get(1));
        assertEquals("alter table users add constraint users_post_id_foreign foreign key (post_id) references post (id)", statements.get(2));
//        System.out.println(statements);
    }
}
