package com.mitchdennett.framework.commands;

public class MigrateCommand extends Command {

    public MigrateCommand() {
        description = "Run Database Migrations";
        command = "migrate";
        addFlag("t", "Table Name");
    }

    @Override
    public void execute(String[] args) {
        try {
            Migrate.runUp(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
