package com.severell.core.commands;

import org.apache.maven.shared.invoker.MavenInvocationException;
import picocli.CommandLine;

@CommandLine.Command(name="migrate:rollback", mixinStandardHelpOptions = true, version = "0.1", description = "Rollback migrations" )
public class MigrateRollbackCommand extends Command{

    @Override
    public void execute() {
        CommandLogger.printlnGreen(String.format("Compiling Migrations..."));
        compile();


        try {
            new Migrate(connection == null ? getConnection() : connection).rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
