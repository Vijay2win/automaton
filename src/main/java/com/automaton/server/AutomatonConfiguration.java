package com.automaton.server;

import java.io.IOError;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import com.google.common.collect.Lists;

/**
 * Simple hierarchical properties reader, generalized across the code base
 */
public class AutomatonConfiguration {
    private static final Properties PROPERTIES = new Properties();
    static {
        try {
            String configFile = "automaton.properties";
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL url = loader.getResource(configFile);
            PROPERTIES.load(url.openStream());
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public static String getString(String name, String defaultVal) {
        String value = System.getProperty(name);
        if (value != null)
            return value;

        return PROPERTIES.getProperty(name, defaultVal);
    }

    public static int getInt(String name, int defaultVal) {
        String existing = getString(name, null);
        if (existing == null)
            return defaultVal;
        return Integer.parseInt(existing);
    }

    public static long getLong(String name, long defaultVal) {
        String existing = getString(name, null);
        if (existing == null)
            return defaultVal;
        return Long.parseLong(existing);
    }

    public static List<String> getStringAsList(String name, List<String> defaultVal) {
        String existing = getString(name, null);
        if (existing == null)
            return defaultVal;
        return Lists.newArrayList(existing.split(","));
    }

    public static boolean getBoolean(String name, boolean defaultVal) {
        String existing = getString(name, null);
        if (existing == null)
            return defaultVal;
        return Boolean.parseBoolean(existing);
    }
}
