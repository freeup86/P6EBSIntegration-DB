package com.p6ebs.integration.ui;

import com.p6ebs.integration.util.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;



    public MainFrame() {
        setTitle("P6-EBS Integration Tool");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create menu bar
        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Add tabs
        tabbedPane.addTab("Dashboard", createIcon("dashboard.png"), new DashboardPanel());
        tabbedPane.addTab("Projects", createIcon("project.png"), new ProjectPanel());
        tabbedPane.addTab("Tasks", createIcon("task.png"), new TaskPanel());
        tabbedPane.addTab("Resources", createIcon("resource.png"), new ResourcePanel());
        tabbedPane.addTab("Logs", createIcon("log.png"), new LogPanel());

        // Add to frame
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        // Add status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel("Ready");
        statusPanel.add(statusLabel, BorderLayout.WEST);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");

        JMenuItem connectionItem = new JMenuItem("Database Connections...");
        connectionItem.addActionListener(e -> openConnectionDialog());
        fileMenu.add(connectionItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            DatabaseManager.shutdown();
            System.exit(0);
        });
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Tools menu
        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem syncItem = new JMenuItem("Start Full Sync");
        syncItem.addActionListener(e -> startFullSync());
        toolsMenu.add(syncItem);

        JMenuItem configItem = new JMenuItem("Configuration");
        configItem.addActionListener(e -> openConfigDialog());
        toolsMenu.add(configItem);
        menuBar.add(toolsMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private ImageIcon createIcon(String iconName) {
        // In a real app, you would load icons from resources
        return null;
    }

    private void startFullSync() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Start a full synchronization between P6 and EBS?",
                "Confirm Full Sync",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            // Code to start full sync
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return new com.p6ebs.integration.dao.ProjectDAO().triggerAllProjectsSync();
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(
                                    MainFrame.this,
                                    "Full synchronization started successfully.",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            JOptionPane.showMessageDialog(
                                    MainFrame.this,
                                    "Failed to start full synchronization.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(
                                MainFrame.this,
                                "Error: " + e.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            worker.execute();
        }
    }

    private void openConfigDialog() {
        // Show configuration dialog
        JOptionPane.showMessageDialog(
                this,
                "Configuration dialog not implemented yet.",
                "Configuration",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
                this,
                "P6-EBS Integration Tool\nVersion 1.0\n\nDeveloped by Your Company",
                "About",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Open the connection dialog
     */
    private void openConnectionDialog() {
        ConnectionDialog dialog = new ConnectionDialog(this);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            statusLabel.setText("Ready - Connected to database");
            JOptionPane.showMessageDialog(
                    this,
                    "Database connection updated successfully.",
                    "Connection Updated",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }


    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Launch application
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}