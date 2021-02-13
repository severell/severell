package com.severell.core.commands;

import org.apache.maven.shared.invoker.MavenInvocationException;
import picocli.CommandLine;

@CommandLine.Command(name="migrate:reset", mixinStandardHelpOptions = true, version = "0.1", description = "Reset migrations" )
public class MigrateResetCommand extends Command{

    @Override
    public void execute() {
        CommandLogger.printlnGreen(String.format("Compiling Migrations..."));
        compile();

        try {
            new Migrate(connection == null ? getConnection() : connection).reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
