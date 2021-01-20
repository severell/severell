package com.severell.core.commands;

import com.severell.core.config.Config;
import com.severell.core.database.Connection;
import com.severell.core.database.ConnectionBuilder;
import com.severell.core.database.PostgresConnection;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public abstract class Command implements Callable<Integer> {

    protected String calleePackage;
    protected Connection connection;

    public abstract void execute() throws IOException;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setCalleePackage(String calleePackage) {
        this.calleePackage = calleePackage;
    }

    protected Connection getConnection() {
        BasicDataSource connectionPool = new BasicDataSource();
        connectionPool.setUsername(Config.get("DB_USERNAME"));
        connectionPool.setPassword(Config.get("DB_PASSWORD"));
        connectionPool.setDriverClassName(Config.get("DB_DRIVER"));
        connectionPool.setUrl(Config.get("DB_CONNSTRING"));
        connectionPool.setInitialSize(1);
        connectionPool.setMinIdle(1);
        connectionPool.setMaxIdle(1);
        Connection connection = ConnectionBuilder.build(Config.get("DB_DRIVER"));
        connection.setDataSource(connectionPool);
        return connection;
    }

    @Override
    public Integer call() {
        try {
            execute();
        } catch (IOException e) {
            CommandLogger.printlnRed("Failed executing command");
            return -1;
        }
        return 0;
    }
}
