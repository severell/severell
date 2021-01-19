package com.severell.core.commands;

import com.severell.core.database.Connection;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name="migrate", mixinStandardHelpOptions = true, version = "0.1", description = "Run migrations" )
public class MigrateCommand extends Command implements Callable<Integer> {


    @Override
    public void execute() {
        try {
            Connection con = connection == null ? getConnection() : connection;
            new Migrate(con).runUp();
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
