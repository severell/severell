package com.severell.core.commands;

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
