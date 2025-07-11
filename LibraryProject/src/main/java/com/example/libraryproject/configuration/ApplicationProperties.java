package com.example.libraryproject.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationProperties {

    private static final Properties properties = new Properties();

    static {

        InputStream input = ApplicationProperties.class.getClassLoader().getResourceAsStream("application.properties");

        try {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static final String ACTIVATION_BASE_URL = System.getenv("ACTIVATION_BASE_URL") != null ?
            System.getenv("ACTIVATION_BASE_URL") : "http://localhost:8080/LibraryProject_war_exploded";

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
