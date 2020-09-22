package com.severell.core.commands;

import com.severell.core.time.Time;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MakeMigrationTest {

    @BeforeAll
    public static void setup() {
        Time.setClock(Clock.fixed(Instant.parse("2020-09-22T10:56:30.00Z"), ZoneId.of("UTC")));
    }

    @AfterAll
    public static void tearDown() {
        Time.resetClock();
    }

    @Test
    public void makeMigrationTestCreate() {
        MakeMigration make = new MakeMigration();
        StringWriter writer = new StringWriter();
        make.setCalleePackage("com.example");
        make.setWriter(writer);
        make.run(new String[]{"args=create_users", "flags=-c=users"});
        assertEquals("package migrations;\n" +
                "\n" +
                "import com.severell.core.database.migrations.Blueprint;\n" +
                "import com.severell.core.database.migrations.MigrationException;\n" +
                "import com.severell.core.database.migrations.Schema;\n" +
                "\n" +
                "public class m_2020_09_22_105630_create_users {\n" +
                "  public static void up() throws MigrationException {\n" +
                "    Schema.create(\"users\", (Blueprint table) -> {\n" +
                "    } );\n" +
                "  }\n" +
                "\n" +
                "  public static void down() throws MigrationException {\n" +
                "  }\n" +
                "}\n", writer.toString());
    }

    @Test
    public void makeMigrationTestUpdate() {
        MakeMigration make = new MakeMigration();
        StringWriter writer = new StringWriter();
        make.setCalleePackage("com.example");
        make.setWriter(writer);
        make.run(new String[]{"args=create_users", "flags=-t=users"});
        assertEquals("package migrations;\n" +
                "\n" +
                "import com.severell.core.database.migrations.Blueprint;\n" +
                "import com.severell.core.database.migrations.MigrationException;\n" +
                "import com.severell.core.database.migrations.Schema;\n" +
                "\n" +
                "public class m_2020_09_22_105630_create_users {\n" +
                "  public static void up() throws MigrationException {\n" +
                "    Schema.table(\"users\", (Blueprint table) -> {\n" +
                "    } );\n" +
                "  }\n" +
                "\n" +
                "  public static void down() throws MigrationException {\n" +
                "  }\n" +
                "}\n", writer.toString());
    }

    @Test
    public void makeMigrationNoFlag() {
        MakeMigration make = new MakeMigration();
        StringWriter writer = new StringWriter();
        make.setCalleePackage("com.example");
        make.setWriter(writer);
        make.run(new String[]{"args=create_users", "flags="});
        assertEquals("package migrations;\n" +
                "\n" +
                "import com.severell.core.database.migrations.Blueprint;\n" +
                "import com.severell.core.database.migrations.MigrationException;\n" +
                "import com.severell.core.database.migrations.Schema;\n" +
                "\n" +
                "public class m_2020_09_22_105630_create_users {\n" +
                "  public static void up() throws MigrationException {\n" +
                "    Schema.table(\"\", (Blueprint table) -> {\n" +
                "    } );\n" +
                "  }\n" +
                "\n" +
                "  public static void down() throws MigrationException {\n" +
                "  }\n" +
                "}\n", writer.toString());
    }
}
