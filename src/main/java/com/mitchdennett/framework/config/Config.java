package com.mitchdennett.framework.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Optional;

public class Config {

    private static Dotenv dotenv;
    private static String dir = "src/main/resources";

    public static void loadConfig() throws Exception {
        if(dotenv != null) {
            throw new Exception("Config has already been loaded");
        }
        dotenv = Dotenv.configure().directory(dir).load();
    }

    protected static void setDir(String directory) {
        dir = directory;
    }

    public static String get(String key) {
        Optional<Dotenv> env = Optional.ofNullable(dotenv);
        return env.isPresent() ? env.get().get(key) :  null;
    }
}
