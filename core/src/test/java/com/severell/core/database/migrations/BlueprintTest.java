package com.severell.core.database.migrations;

import com.severell.core.database.grammar.MySQLGrammer;
import com.severell.core.database.grammar.PostgresGrammar;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BlueprintTest {

    @Test
    public void indexOnAnExistingTable() {
        for (com.severell.core.database.grammar.Grammar grammar : List.of(new PostgresGrammar(), new MySQLGrammer())) {
            Blueprint table = new Blueprint("sessions");
            table.index("expires_at");
            assertEquals(List.of("create index sessions_expires_at_index on sessions (expires_at)"), table.compileCommands(grammar));
        }
    }

    @Test
    public void indexAlongsideAddedColumns() {
        Blueprint table = new Blueprint("sessions");
        table.timestamp("created_at").nullable();
        table.index("expires_at");
        List<String> sql = table.compileCommands(new PostgresGrammar());
        assertEquals(2, sql.size());
        assertTrue(sql.get(0).startsWith("create index sessions_expires_at_index"), sql.toString());
        assertTrue(sql.get(1).contains("created_at"), sql.toString());
    }
}
