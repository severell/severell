package com.severell.core.commands;

import com.severell.core.config.Config;
import com.severell.core.database.Connection;
import com.severell.core.database.PostgresConnection;
import org.apache.commons.dbcp2.BasicDataSource;

public class MigrateCommand extends Command {

    public MigrateCommand() {
        description = "Run Database Migrations";
        command = "migrate";
        addFlag("t", "Table Name");
    }

    @Override
    public void execute(String[] args) {
        try {
            Connection con = connection == null ? getConnection() : connection;
            new Migrate(con).runUp(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Connection getConnection () {
        BasicDataSource connectionPool = new BasicDataSource();
        connectionPool.setUsername(Config.get("DB_USERNAME"));
        connectionPool.setPassword(Config.get("DB_PASSWORD"));
        connectionPool.setDriverClassName(Config.get("DB_DRIVER"));
        connectionPool.setUrl(Config.get("DB_CONNSTRING"));
        connectionPool.setInitialSize(1);
        connectionPool.setMinIdle(1);
        connectionPool.setMaxIdle(1);
        Connection connection = new PostgresConnection();
        connection.setDataSource(connectionPool);
        return connection;
    }
}
