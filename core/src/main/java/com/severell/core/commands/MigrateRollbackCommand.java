package com.severell.core.commands;

import com.severell.core.config.Config;
import com.severell.core.database.Connection;
import com.severell.core.database.PostgresConnection;
import org.apache.commons.dbcp2.BasicDataSource;

public class MigrateRollbackCommand extends Command{

    public MigrateRollbackCommand() {
        description = "Rollback Database Migrations";
        command = "migrate:rollback";
    }

    @Override
    public void execute(String[] args) {
        try {
            new Migrate(connection == null ? getConnection() : connection).rollback(args);
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
