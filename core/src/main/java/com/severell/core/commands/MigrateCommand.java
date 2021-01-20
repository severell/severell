package com.severell.core.commands;

import com.severell.core.database.Connection;
import picocli.CommandLine;

@CommandLine.Command(name="migrate", mixinStandardHelpOptions = true, version = "0.1", description = "Run migrations" )
public class MigrateCommand extends Command {


    @Override
    public void execute() {
        try {
            Connection con = connection == null ? getConnection() : connection;
            new Migrate(con).runUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
