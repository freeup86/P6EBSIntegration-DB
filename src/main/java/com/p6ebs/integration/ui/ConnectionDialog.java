package com.p6ebs.integration.ui;

import com.p6ebs.integration.model.ConnectionSettings;
import com.p6ebs.integration.util.ConnectionSettingsManager;
import com.p6ebs.integration.util.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Dialog for configuring database connection settings
 */
public class ConnectionDialog extends JDialog {
    private JTextField p6UrlField;
    private JTextField p6UsernameField;
    private JPasswordField p6PasswordField;
    private JTextField ebsUrlField;
    private JTextField ebsUsernameField;
    private JPasswordField ebsPasswordField;
    private JTextField schemaField;

    private JButton testP6Button;
    private JButton testEbsButton;
    private JButton saveButton;
    private JButton cancelButton;

    private boolean confirmed = false;
    private ConnectionSettings settings;

    /**
     * Constructor
     */
    public ConnectionDialog(Frame owner) {
        super(owner, "Database Connection Settings", true);
        settings = ConnectionSettingsManager.loadSettings();
        initComponents();
        loadSettings();
    }

    /**
     * Initialize UI components
     */
    private void initComponents() {
        setSize(600, 400);
        setLocationRelativeTo(getOwner());

        setLayout(new BorderLayout());

        // Main panel with form fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // P6 Connection section
        addSectionHeader(formPanel, "Primavera P6 Connection", gbc, 0);

        gbc.gridy++;
        addLabel(formPanel, "JDBC URL:", gbc, 0);
        p6UrlField = addTextField(formPanel, gbc, 1);

        gbc.gridy++;
        addLabel(formPanel, "Username:", gbc, 0);
        p6UsernameField = addTextField(formPanel, gbc, 1);

        gbc.gridy++;
        addLabel(formPanel, "Password:", gbc, 0);
        p6PasswordField = addPasswordField(formPanel, gbc, 1);

        gbc.gridy++;
        gbc.gridx = 1;
        testP6Button = new JButton("Test P6 Connection");
        testP6Button.addActionListener(e -> testP6Connection());
        formPanel.add(testP6Button, gbc);

        // EBS Connection section
        gbc.gridx = 0;
        gbc.gridy++;
        addSectionHeader(formPanel, "Oracle EBS Connection", gbc, 0);

        gbc.gridy++;
        addLabel(formPanel, "JDBC URL:", gbc, 0);
        ebsUrlField = addTextField(formPanel, gbc, 1);

        gbc.gridy++;
        addLabel(formPanel, "Username:", gbc, 0);
        ebsUsernameField = addTextField(formPanel, gbc, 1);

        gbc.gridy++;
        addLabel(formPanel, "Password:", gbc, 0);
        ebsPasswordField = addPasswordField(formPanel, gbc, 1);

        gbc.gridy++;
        gbc.gridx = 1;
        testEbsButton = new JButton("Test EBS Connection");
        testEbsButton.addActionListener(e -> testEbsConnection());
        formPanel.add(testEbsButton, gbc);

        // Integration Schema section
        gbc.gridx = 0;
        gbc.gridy++;
        addSectionHeader(formPanel, "Integration Settings", gbc, 0);

        gbc.gridy++;
        addLabel(formPanel, "Integration Schema:", gbc, 0);
        schemaField = addTextField(formPanel, gbc, 1);

        // Add form panel to dialog
        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save and Connect");
        saveButton.addActionListener(e -> saveSettings());
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> cancel());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Handle window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        // Set default button
        getRootPane().setDefaultButton(saveButton);
    }

    /**
     * Add a section header to the form
     */
    private void addSectionHeader(JPanel panel, String text, GridBagConstraints gbc, int gridx) {
        gbc.gridx = gridx;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);

        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        panel.add(label, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
    }

    /**
     * Add a label to the form
     */
    private void addLabel(JPanel panel, String text, GridBagConstraints gbc, int gridx) {
        gbc.gridx = gridx;
        gbc.weightx = 0.0;
        panel.add(new JLabel(text), gbc);
    }

    /**
     * Add a text field to the form
     */
    private JTextField addTextField(JPanel panel, GridBagConstraints gbc, int gridx) {
        gbc.gridx = gridx;
        gbc.weightx = 1.0;
        JTextField field = new JTextField(30);
        panel.add(field, gbc);
        return field;
    }

    /**
     * Add a password field to the form
     */
    private JPasswordField addPasswordField(JPanel panel, GridBagConstraints gbc, int gridx) {
        gbc.gridx = gridx;
        gbc.weightx = 1.0;
        JPasswordField field = new JPasswordField(30);
        panel.add(field, gbc);
        return field;
    }

    /**
     * Load settings into form fields
     */
    private void loadSettings() {
        p6UrlField.setText(settings.getP6Url());
        p6UsernameField.setText(settings.getP6Username());
        p6PasswordField.setText(settings.getP6Password());
        ebsUrlField.setText(settings.getEbsUrl());
        ebsUsernameField.setText(settings.getEbsUsername());
        ebsPasswordField.setText(settings.getEbsPassword());
        schemaField.setText(settings.getIntegrationSchema());
    }

    /**
     * Save settings from form fields
     */
    private void saveSettings() {
        // Update settings from form fields
        settings.setP6Url(p6UrlField.getText().trim());
        settings.setP6Username(p6UsernameField.getText().trim());
        settings.setP6Password(new String(p6PasswordField.getPassword()));
        settings.setEbsUrl(ebsUrlField.getText().trim());
        settings.setEbsUsername(ebsUsernameField.getText().trim());
        settings.setEbsPassword(new String(ebsPasswordField.getPassword()));
        settings.setIntegrationSchema(schemaField.getText().trim());

        // Save to file
        ConnectionSettingsManager.saveSettings(settings);

        // Initialize database connections
        try {
            DatabaseManager.initialize(settings);
            confirmed = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error initializing database connections: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Test P6 connection
     */
    private void testP6Connection() {
        // Update settings with current values
        settings.setP6Url(p6UrlField.getText().trim());
        settings.setP6Username(p6UsernameField.getText().trim());
        settings.setP6Password(new String(p6PasswordField.getPassword()));

        // Initialize and test
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Create temporary connection settings with only P6 details
            ConnectionSettings tempSettings = new ConnectionSettings();
            tempSettings.setP6Url(settings.getP6Url());
            tempSettings.setP6Username(settings.getP6Username());
            tempSettings.setP6Password(settings.getP6Password());

            DatabaseManager.initialize(tempSettings);
            boolean success = DatabaseManager.testP6Connection();

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "P6 connection test successful!",
                        "Connection Test",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "P6 connection test failed.",
                        "Connection Test",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error testing P6 connection: " + e.getMessage(),
                    "Connection Test",
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Test EBS connection
     */
    private void testEbsConnection() {
        // Update settings with current values
        settings.setEbsUrl(ebsUrlField.getText().trim());
        settings.setEbsUsername(ebsUsernameField.getText().trim());
        settings.setEbsPassword(new String(ebsPasswordField.getPassword()));

        // Initialize and test
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Create temporary connection settings with only EBS details
            ConnectionSettings tempSettings = new ConnectionSettings();
            tempSettings.setEbsUrl(settings.getEbsUrl());
            tempSettings.setEbsUsername(settings.getEbsUsername());
            tempSettings.setEbsPassword(settings.getEbsPassword());

            DatabaseManager.initialize(tempSettings);
            boolean success = DatabaseManager.testEBSConnection();

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "EBS connection test successful!",
                        "Connection Test",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "EBS connection test failed.",
                        "Connection Test",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error testing EBS connection: " + e.getMessage(),
                    "Connection Test",
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Cancel the dialog
     */
    private void cancel() {
        confirmed = false;
        dispose();
    }

    /**
     * Check if the dialog was confirmed
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Get the connection settings
     */
    public ConnectionSettings getSettings() {
        return settings;
    }
}