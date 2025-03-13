package com.p6ebs.integration.util;

import com.p6ebs.integration.model.ConnectionSettings;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages database connections to P6 and EBS databases
 */
public class DatabaseManager {
    private static BasicDataSource p6DataSource;
    private static BasicDataSource ebsDataSource;
    private static ConnectionSettings connectionSettings;
    private static boolean isInitialized = false;

    // Private constructor to prevent instantiation
    private DatabaseManager() {}

    /**
     * Initialize database connections with the provided settings
     */
    public static void initialize(ConnectionSettings settings) {
        System.out.println("Initializing database connections");
        connectionSettings = settings;

        // Close existing data sources if they exist
        closeDataSource(p6DataSource);
        closeDataSource(ebsDataSource);

        // Initialize P6 data source
        p6DataSource = new BasicDataSource();
        p6DataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        p6DataSource.setUrl(settings.getP6Url());
        p6DataSource.setUsername(settings.getP6Username());
        p6DataSource.setPassword(settings.getP6Password());
        p6DataSource.setInitialSize(1);
        p6DataSource.setMaxTotal(5);

        // Initialize EBS data source
        ebsDataSource = new BasicDataSource();
        ebsDataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        ebsDataSource.setUrl(settings.getEbsUrl());
        ebsDataSource.setUsername(settings.getEbsUsername());
        ebsDataSource.setPassword(settings.getEbsPassword());
        ebsDataSource.setInitialSize(1);
        ebsDataSource.setMaxTotal(5);

        isInitialized = true;
        System.out.println("Database connections initialized");
    }

    /**
     * Get a connection to the P6 database
     */
    public static Connection getP6Connection() throws SQLException {
        if (!isInitialized) {
            throw new SQLException("Database connection not initialized. Please set up connection settings first.");
        }
        return p6DataSource.getConnection();
    }

    /**
     * Get a connection to the EBS database
     */
    public static Connection getEBSConnection() throws SQLException {
        if (!isInitialized) {
            throw new SQLException("Database connection not initialized. Please set up connection settings first.");
        }
        return ebsDataSource.getConnection();
    }

    /**
     * Close database resources
     */
    public static void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    System.err.println("Error closing resource: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Close a data source
     */
    private static void closeDataSource(BasicDataSource dataSource) {
        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (SQLException e) {
                System.err.println("Error closing data source: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if database connections are initialized
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Get the current connection settings
     */
    public static ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    /**
     * Shutdown all database connections
     */
    public static void shutdown() {
        System.out.println("Shutting down database connections");
        closeDataSource(p6DataSource);
        closeDataSource(ebsDataSource);
        isInitialized = false;
    }

    /**
     * Test P6 database connection
     */
    public static boolean testP6Connection() {
        if (!isInitialized) {
            return false;
        }

        Connection conn = null;
        try {
            System.out.println("Testing P6 connection...");
            conn = p6DataSource.getConnection();
            boolean valid = conn.isValid(5);
            System.out.println("P6 connection test " + (valid ? "successful" : "failed"));
            return valid;
        } catch (SQLException e) {
            System.err.println("P6 connection test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(conn);
        }
    }

    /**
     * Test EBS database connection
     */
    public static boolean testEBSConnection() {
        if (!isInitialized) {
            return false;
        }

        Connection conn = null;
        try {
            System.out.println("Testing EBS connection...");
            conn = ebsDataSource.getConnection();
            boolean valid = conn.isValid(5);
            System.out.println("EBS connection test " + (valid ? "successful" : "failed"));
            return valid;
        } catch (SQLException e) {
            System.err.println("EBS connection test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(conn);
        }
    }
}