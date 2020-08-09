package com.mitchdennett.framework.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Optional;

public class Config {

    private static Dotenv dotenv;

    public static void loadConfig() throws Exception {
        if(dotenv != null) {
            throw new Exception("Config has already been loaded");
        }
        dotenv = Dotenv.load();
    }

    public static String get(String key) {
        Optional<Dotenv> env = Optional.ofNullable(dotenv);
        return env.isPresent() ? env.get().get(key) :  null;
    }
}
