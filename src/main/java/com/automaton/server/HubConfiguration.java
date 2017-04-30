package com.automaton.server;

import java.io.IOError;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import com.google.common.collect.Lists;

public class HubConfiguration {
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
        return PROPERTIES.getProperty(name, defaultVal);
    }

    public static int getInt(String name, int defaultVal) {
        String existing = PROPERTIES.getProperty(name);
        if (existing == null)
            return defaultVal;
        return Integer.parseInt(existing);
    }

    public static long getLong(String name, long defaultVal) {
        String existing = PROPERTIES.getProperty(name);
        if (existing == null)
            return defaultVal;
        return Long.parseLong(existing);
    }

    public static List<String> getStringAsList(String name, List<String> defaultVal) {
        String existing = PROPERTIES.getProperty(name);
        if (existing == null)
            return defaultVal;
        return Lists.newArrayList(existing.split(","));
    }
}
