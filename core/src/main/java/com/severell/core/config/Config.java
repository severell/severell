package com.severell.core.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Optional;

public class Config {

    private static Dotenv dotenv;
    private static String dir;

    public static void loadConfig() throws Exception {
        if(dotenv != null) {
            throw new Exception("Config has already been loaded");
        }
        if(dir != null) {
            dotenv = Dotenv.configure().directory(dir).load();
        } else {
            dotenv = Dotenv.configure().load();
        }

    }

    public static void unload() {
        if(dotenv != null) {
            dotenv = null;
        }
    }

    public static boolean isLoaded() {
        return dotenv != null;
    }

    public static void setDir(String directory) {
        dir = directory;
    }

    public static String get(String key) {
        Optional<Dotenv> env = Optional.ofNullable(dotenv);
        return env.isPresent() ? env.get().get(key) :  null;
    }

    public static String get(String key, String defaultValue) {
        return get(key) == null ? defaultValue : get(key);
    }

    public static boolean equals(String key, String expected) {
        Optional<Dotenv> env = Optional.ofNullable(dotenv);
        if(env.isPresent()){
            Optional<String> val = Optional.ofNullable(env.get().get(key));
            String actualVal = val.isPresent() ? val.get() : "";
            return actualVal.equals(expected);
        }
        return false;
    }

    public static boolean isSet(String key) {
        String value = get(key);
        return !(value == null || value.isEmpty());
    }

    public static boolean isLocal() {
        return equals("ENV", "TEST");
    }
}
