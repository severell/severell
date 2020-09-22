package com.severell.core.commands;

import com.severell.core.config.Config;
import com.severell.core.database.Connection;
import com.severell.core.database.PostgresConnection;
import org.apache.commons.dbcp2.BasicDataSource;

public class MigrateRollbackCommand extends Command{

    public MigrateRollbackCommand() {
        description = "Rollback Database Migrations";
        command = "migrate:rollback";
    }

    @Override
    public void execute(String[] args) {
        try {
            new Migrate(connection == null ? getConnection() : connection).rollback(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
