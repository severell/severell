package com.severell.core.commands;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name="migrate:reset", mixinStandardHelpOptions = true, version = "0.1", description = "Reset migrations" )
public class MigrateResetCommand extends Command implements Callable<Integer> {

    @Override
    public void execute() {
        try {
            new Migrate(connection == null ? getConnection() : connection).reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer call() throws Exception {
        execute();
        return 0;
    }
}
