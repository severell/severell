package com.severell.core.commands;

import org.apache.maven.shared.invoker.MavenInvocationException;
import picocli.CommandLine;

@CommandLine.Command(name="migrate:reset", mixinStandardHelpOptions = true, version = "0.1", description = "Reset migrations" )
public class MigrateResetCommand extends Command{

    @Override
    public void execute() {
        try {
            CommandLogger.printlnGreen(String.format("Compiling Migrations..."));
            compile();
        } catch (MavenInvocationException e) {
            //Implement Proper Logging Here.
            e.printStackTrace();
        }

        try {
            new Migrate(connection == null ? getConnection() : connection).reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
