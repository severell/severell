package com.severell.core.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Optional;

/**
 * The Config class is used to hold all the environment variables for the application
 */
public class Config {

    private static Dotenv dotenv;
    private static String dir;

    /**
     * Load the contents of the .env file and system environment variables
     * NOTE: The system environment variables take precedence over the.env file
     * @throws Exception if the config file has already been loaded
     */
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

    /**
     * Clear out the environment variables
     */
    public static void unload() {
        if(dotenv != null) {
            dotenv = null;
        }
    }

    /**
     * Check if the environment file has been loaded
     * @return true if environment file has been loaded
     */
    public static boolean isLoaded() {
        return dotenv != null;
    }

    /**
     * Set the directory where the .env file is located
     * @param directory
     */
    public static void setDir(String directory) {
        dir = directory;
    }

    /**
     * Get an environment variable
     * @param key Name of the environment variable
     * @return the environment variable
     */
    public static String get(String key) {
        Optional<Dotenv> env = Optional.ofNullable(dotenv);
        return env.isPresent() ? env.get().get(key) :  null;
    }

    /**
     * Get an environment variable but if its not set return the default value
     * @param key Name of the environment variable
     * @param defaultValue return this if not set
     * @return
     */
    public static String get(String key, String defaultValue) {
        return get(key) == null ? defaultValue : get(key);
    }

    /**
     * Checks whether the environment variable is equal to the expected value
     * @param key Name of the environment variable
     * @param expected Expected value
     * @return
     */
    public static boolean equals(String key, String expected) {
        Optional<Dotenv> env = Optional.ofNullable(dotenv);
        if(env.isPresent()){
            Optional<String> val = Optional.ofNullable(env.get().get(key));
            String actualVal = val.isPresent() ? val.get() : "";
            return actualVal.equals(expected);
        }
        return false;
    }

    /**
     * Check whether an environment variable is set. Returns false if value is null or empty
     * @param key Name of the environment variable
     * @return boolean - true if variable is set
     */
    public static boolean isSet(String key) {
        String value = get(key);
        return !(value == null || value.isEmpty());
    }

    /**
     * Returns true if the ENV variable is set to TEST
     * @return Returns true if the ENV variable is set to TEST
     */
    public static boolean isLocal() {
        return equals("ENV", "TEST");
    }
}
