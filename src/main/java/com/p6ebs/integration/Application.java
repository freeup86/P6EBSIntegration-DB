package com.p6ebs.integration;

import com.p6ebs.integration.ui.MainFrame;

import javax.swing.*;

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
            // Create and show main frame
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}