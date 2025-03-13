package com.p6ebs.integration;

import com.p6ebs.integration.model.ConnectionSettings;
import com.p6ebs.integration.ui.ConnectionDialog;
import com.p6ebs.integration.ui.MainFrame;
import com.p6ebs.integration.util.ConnectionSettingsManager;
import com.p6ebs.integration.util.DatabaseManager;

import javax.swing.*;
import java.awt.*;

/**
 * Main application class
 */
public class Application {
    /**
     * Application entry point
     */
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Launch application
        SwingUtilities.invokeLater(() -> {
            // Try to load saved settings
            ConnectionSettings savedSettings = ConnectionSettingsManager.loadSettings();
            boolean showConnectionDialog = true;

            // Try to initialize with saved settings first
            try {
                DatabaseManager.initialize(savedSettings);

                // Test connections
                boolean p6Success = DatabaseManager.testP6Connection();
                boolean ebsSuccess = DatabaseManager.testEBSConnection();

                if (p6Success && ebsSuccess) {
                    // Connections successful, no need to show dialog
                    showConnectionDialog = false;
                }
            } catch (Exception e) {
                System.err.println("Failed to initialize with saved settings: " + e.getMessage());
                // Will show connection dialog
            }

            if (showConnectionDialog) {
                // Create a temporary frame to serve as owner for the dialog
                JFrame tempFrame = new JFrame();
                tempFrame.setSize(100, 100);
                tempFrame.setLocationRelativeTo(null);
                tempFrame.setUndecorated(true);
                tempFrame.setVisible(true);

                // Show connection dialog
                ConnectionDialog connectionDialog = new ConnectionDialog(tempFrame);
                connectionDialog.setVisible(true);

                // Clean up temporary frame
                tempFrame.dispose();

                // Exit if dialog was cancelled
                if (!connectionDialog.isConfirmed()) {
                    System.exit(0);
                }
            }

            // Create and show main frame
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}