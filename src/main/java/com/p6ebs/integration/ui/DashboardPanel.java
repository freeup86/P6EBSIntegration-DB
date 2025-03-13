package com.p6ebs.integration.ui;

import com.p6ebs.integration.dao.ProjectDAO;
import com.p6ebs.integration.model.Project;
import com.p6ebs.integration.util.LoggerUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import org.apache.logging.log4j.Logger;

public class DashboardPanel extends JPanel {
    private static final Logger logger = LoggerUtil.getLogger(DashboardPanel.class);

    private JTable recentSyncTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton syncAllButton;
    private JLabel statusLabel;

    public DashboardPanel() {
        setLayout(new BorderLayout());

        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("P6-EBS Integration Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        buttonPanel.add(refreshButton);

        syncAllButton = new JButton("Sync All Projects");
        syncAllButton.addActionListener(e -> syncAllProjects());
        buttonPanel.add(syncAllButton);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create recent synchronizations panel
        JPanel recentSyncPanel = new JPanel(new BorderLayout());
        recentSyncPanel.setBorder(BorderFactory.createTitledBorder("Recent Synchronizations"));

        String[] columnNames = {"Project ID", "Project Name", "Status", "Last Sync", "Error Message"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recentSyncTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(recentSyncTable);
        recentSyncPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(recentSyncPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Add status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel("Ready");
        statusPanel.add(statusLabel, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);

        // Add sync statistics panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 10, 10),
                BorderFactory.createTitledBorder("Synchronization Statistics")
        ));

        statsPanel.add(createStatsCard("Projects", "0"));
        statsPanel.add(createStatsCard("Tasks", "0"));
        statsPanel.add(createStatsCard("Resources", "0"));
        statsPanel.add(createStatsCard("Errors", "0"));

        mainPanel.add(statsPanel, BorderLayout.NORTH);

        // Load initial data
        refreshData();
    }

    private JPanel createStatsCard(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(titleLabel, BorderLayout.NORTH);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(valueLabel, BorderLayout.CENTER);

        return panel;
    }

    private void refreshData() {
        logger.info("Refreshing dashboard data");
        statusLabel.setText("Loading data...");
        tableModel.setRowCount(0);

        SwingWorker<List<Project>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Project> doInBackground() throws Exception {
                return new ProjectDAO().getStagingProjects();
            }

            @Override
            protected void done() {
                try {
                    List<Project> projects = get();
                    int successCount = 0;
                    int pendingCount = 0;
                    int errorCount = 0;

                    for (Project project : projects) {
                        Object[] row = {
                                project.getId(),
                                project.getName(),
                                project.getSyncStatus(),
                                project.getStartDate(), // Using start date as proxy for last sync date
                                project.getErrorMessage()
                        };
                        tableModel.addRow(row);

                        // Count by status
                        if (project.getSyncStatus() != null) {
                            if (project.getSyncStatus().contains("SUCCESS")) {
                                successCount++;
                            } else if (project.getSyncStatus().contains("ERROR")) {
                                errorCount++;
                            } else {
                                pendingCount++;
                            }
                        }
                    }

                    // Update stats panels
                    updateStatsPanel(0, String.valueOf(projects.size()));
                    updateStatsPanel(3, String.valueOf(errorCount));

                    // Fetch task count (simplified - would need a real method in your DAO)
                    SwingWorker<Integer, Void> taskWorker = new SwingWorker<>() {
                        @Override
                        protected Integer doInBackground() throws Exception {
                            return 42; // Placeholder - implement actual count in DAO
                        }

                        @Override
                        protected void done() {
                            try {
                                updateStatsPanel(1, get().toString());
                            } catch (Exception e) {
                                logger.error("Error getting task count", e);
                                updateStatsPanel(1, "Error");
                            }
                        }
                    };
                    taskWorker.execute();

                    // Fetch resource count (simplified)
                    SwingWorker<Integer, Void> resourceWorker = new SwingWorker<>() {
                        @Override
                        protected Integer doInBackground() throws Exception {
                            return 17; // Placeholder - implement actual count in DAO
                        }

                        @Override
                        protected void done() {
                            try {
                                updateStatsPanel(2, get().toString());
                            } catch (Exception e) {
                                logger.error("Error getting resource count", e);
                                updateStatsPanel(2, "Error");
                            }
                        }
                    };
                    resourceWorker.execute();

                    statusLabel.setText("Ready - Last refresh: " + new java.util.Date());
                    logger.info("Dashboard data refreshed successfully");

                    if (projects.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                DashboardPanel.this,
                                "No recent synchronizations found.",
                                "Information",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    logger.error("Error refreshing dashboard data", e);
                    statusLabel.setText("Error loading data - " + e.getMessage());
                    JOptionPane.showMessageDialog(
                            DashboardPanel.this,
                            "Error loading dashboard data: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );

                    // Update stats panels to show error
                    updateStatsPanel(0, "Error");
                    updateStatsPanel(1, "Error");
                    updateStatsPanel(2, "Error");
                    updateStatsPanel(3, "Error");
                }
            }
        };

        worker.execute();
    }

    private void updateStatsPanel(int index, String value) {
        try {
            // Find the stats panel - be more careful about component types
            Component[] components = getComponents();
            JPanel mainPanel = null;

            // First, find the main panel
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    mainPanel = (JPanel) comp;
                    break;
                }
            }

            if (mainPanel == null) {
                System.err.println("Could not find main panel");
                return;
            }

            // Now find the stats panel within the main panel
            JPanel statsPanel = null;
            for (Component comp : mainPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    // This should be the stats panel
                    statsPanel = (JPanel) comp;
                    break;
                }
            }

            if (statsPanel == null) {
                System.err.println("Could not find stats panel");
                return;
            }

            // Make sure index is valid
            if (index < 0 || index >= statsPanel.getComponentCount()) {
                System.err.println("Invalid stats panel index: " + index);
                return;
            }

            // Get the stats card and update its value
            Component cardComp = statsPanel.getComponent(index);
            if (cardComp instanceof JPanel) {
                JPanel card = (JPanel) cardComp;

                // Find the value label within the card
                for (Component comp : card.getComponents()) {
                    if (comp instanceof JLabel) {
                        JLabel label = (JLabel) comp;
                        if (label.getFont().getSize() >= 20) {  // Assume the larger font is the value
                            label.setText(value);
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating stats panel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void syncAllProjects() {
        logger.info("Starting full project synchronization");
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Start synchronization for all projects?\nThis may take some time.",
                "Confirm Full Sync",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            statusLabel.setText("Starting full synchronization...");
            syncAllButton.setEnabled(false);

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return new ProjectDAO().triggerAllProjectsSync();
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            logger.info("Full synchronization started successfully");
                            statusLabel.setText("Full synchronization started");
                            JOptionPane.showMessageDialog(
                                    DashboardPanel.this,
                                    "Full synchronization started successfully.",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                            // Refresh after a short delay to show updated status
                            Timer timer = new Timer(3000, e -> {
                                refreshData();
                                syncAllButton.setEnabled(true);
                            });
                            timer.setRepeats(false);
                            timer.start();
                        } else {
                            logger.error("Failed to start full synchronization");
                            statusLabel.setText("Failed to start full synchronization");
                            syncAllButton.setEnabled(true);
                            JOptionPane.showMessageDialog(
                                    DashboardPanel.this,
                                    "Failed to start full synchronization.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    } catch (Exception e) {
                        logger.error("Error in full synchronization", e);
                        statusLabel.setText("Error: " + e.getMessage());
                        syncAllButton.setEnabled(true);
                        JOptionPane.showMessageDialog(
                                DashboardPanel.this,
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
}