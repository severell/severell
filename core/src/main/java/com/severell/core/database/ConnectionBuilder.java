package com.severell.core.database;

public class ConnectionBuilder {

    public static Connection build(String driver) {
        if(driver == null) {
            return null;
        }

        switch (driver.toLowerCase()) {
            case "org.postgresql.driver":
                return new PostgresConnection();
            case "com.mysql.cj.jdbc.driver":
            case "com.mysql.jdbc.driver":
                return new MySQLConnection();
            default:
                return null;
        }
    }
}
