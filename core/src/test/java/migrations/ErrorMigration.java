package migrations;


import com.severell.core.database.migrations.MigrationException;

public class ErrorMigration {
    public static void up() throws MigrationException {
        throw new MigrationException(MigrationException.MigrationExceptionType.UNKNOWN);
    }

    public static void down() throws MigrationException {
        throw new MigrationException(MigrationException.MigrationExceptionType.UNKNOWN);
    }
}
