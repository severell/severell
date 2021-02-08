package com.severell.core.commands;

import com.severell.core.database.Connection;
import org.apache.maven.shared.invoker.MavenInvocationException;
import picocli.CommandLine;

@CommandLine.Command(name="migrate", mixinStandardHelpOptions = true, version = "0.1", description = "Run migrations" )
public class MigrateCommand extends Command {


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
            Connection con = connection == null ? getConnection() : connection;
            new Migrate(con).runUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
