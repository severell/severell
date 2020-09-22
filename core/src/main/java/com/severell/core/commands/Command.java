package com.severell.core.commands;

import com.severell.core.config.Config;
import com.severell.core.database.Connection;
import com.severell.core.database.PostgresConnection;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.util.ArrayList;

public abstract class Command {

    protected String description;
    protected String command;
    protected int numArgs = -1;
    protected ArrayList<Flag> flags;
    protected String calleePackage;
    protected Connection connection;

    public abstract void execute(String[] args) throws IOException;

    protected boolean validate(String[] args) {
        return true;
    }

    public void run(String[] args) {
        //TODO CLEAN THIS UP A LITTLE BIT. I DON'T LIKE IT A WHOLE LOT
        String[] argsToPass = new String[0];
        if(args.length > 1) {
            String[] argParts = args[0].split("args=");
            if(argParts.length > 0 && argParts[1] != null) {
                argsToPass = argParts[1].split(",");
            }
            parseFlags(args[1]);
        }

        if(validate(argsToPass)) {
            try {
                this.execute(argsToPass);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseFlags(String arg1) {
        String[] flags;
        String[] flagParts = arg1.split("flags=");
        if(flagParts.length > 0 &&  flagParts[1] != null) {
            flags = flagParts[1].split(",");
            for(String f : flags) {
                String name = f.split("=")[0];
                String val = f.split("=")[1];
                for(Flag fl : this.getFlags()) {
                    if(name.substring(1).equals(fl.flag)) {
                        fl.value = val;
                    }
                }
            }
        }
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<Flag> getFlags() {
        return flags == null ? new ArrayList<>() : flags;
    }

    public int getNumArgs(){
        return numArgs;
    }

    public void addFlag(String flag, String description) {
        if(flags == null) {
            this.flags = new ArrayList<>();
        }

        this.flags.add(new Flag(flag, description));
    }

    public void addFlag(Flag flag) {
        if(flags == null) {
            this.flags = new ArrayList<>();
        }

        this.flags.add(flag);
    }

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
        Connection connection = new PostgresConnection();
        connection.setDataSource(connectionPool);
        return connection;
    }
}
