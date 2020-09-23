package com.severell.core.commands;

public class MigrateResetCommand extends Command {


    public MigrateResetCommand() {
        description = "Reset Database Migrations";
        command = "migrate:reset";
    }

    @Override
    public void execute(String[] args) {
        try {
            new Migrate(connection == null ? getConnection() : connection).reset(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
