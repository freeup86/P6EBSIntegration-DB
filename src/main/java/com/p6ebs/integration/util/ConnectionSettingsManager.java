package com.p6ebs.integration.util;

import com.p6ebs.integration.model.ConnectionSettings;

import java.io.*;
import java.util.Properties;

/**
 * Manages saving and loading connection settings to/from a properties file
 */
public class ConnectionSettingsManager {
    private static final String SETTINGS_FILE = "config/connection_settings.properties";

    /**
     * Save connection settings to a properties file
     */
    public static void saveSettings(ConnectionSettings settings) {
        Properties props = new Properties();
        props.setProperty("p6.db.url", settings.getP6Url());
        props.setProperty("p6.db.username", settings.getP6Username());
        props.setProperty("p6.db.password", settings.getP6Password()); // Note: passwords are stored in plain text - not secure
        props.setProperty("ebs.db.url", settings.getEbsUrl());
        props.setProperty("ebs.db.username", settings.getEbsUsername());
        props.setProperty("ebs.db.password", settings.getEbsPassword());
        props.setProperty("integration.schema", settings.getIntegrationSchema());

        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        try (OutputStream output = new FileOutputStream(SETTINGS_FILE)) {
            props.store(output, "P6-EBS Integration Connection Settings");
            System.out.println("Settings saved to " + SETTINGS_FILE);
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load connection settings from a properties file
     */
    public static ConnectionSettings loadSettings() {
        ConnectionSettings settings = new ConnectionSettings();

        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            System.out.println("Settings file not found, using defaults");
            return settings; // Return default settings
        }

        Properties props = new Properties();
        try (InputStream input = new FileInputStream(SETTINGS_FILE)) {
            props.load(input);

            settings.setP6Url(props.getProperty("p6.db.url", settings.getP6Url()));
            settings.setP6Username(props.getProperty("p6.db.username", settings.getP6Username()));
            settings.setP6Password(props.getProperty("p6.db.password", settings.getP6Password()));
            settings.setEbsUrl(props.getProperty("ebs.db.url", settings.getEbsUrl()));
            settings.setEbsUsername(props.getProperty("ebs.db.username", settings.getEbsUsername()));
            settings.setEbsPassword(props.getProperty("ebs.db.password", settings.getEbsPassword()));
            settings.setIntegrationSchema(props.getProperty("integration.schema", settings.getIntegrationSchema()));

            System.out.println("Settings loaded from " + SETTINGS_FILE);
        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
            e.printStackTrace();
        }

        return settings;
    }
}