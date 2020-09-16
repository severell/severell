package migrations;

import com.severell.core.database.migrations.Blueprint;
import com.severell.core.database.migrations.MigrationException;
import com.severell.core.database.migrations.Schema;

public class m_2020_08_24_000000_create_users {

	public static void up() throws MigrationException {
        Schema.create("users", (Blueprint table) -> {
            table.id();
            table.string("name");
            table.string("email");
            table.string("password");
            table.timestamp("verified_at").nullable();
            table.timestamps();
        });
    }

    public static void down() throws MigrationException {
        Schema.drop("users");
    }
	
}