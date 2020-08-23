package com.mitchdennett.framework.commands;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class Commander {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            switch (args[0]) {
                case "migrate":
                    Migrate.runUp(args);
                    break;
                case "migrate:reset":
                    Migrate.reset(args);
                    break;
                case "migrate:rollback":
                    Migrate.rollback(args);
                    break;
            }
        }

    }
}

