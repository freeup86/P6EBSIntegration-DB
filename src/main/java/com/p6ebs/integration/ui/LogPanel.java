package com.p6ebs.integration.ui;

import com.p6ebs.integration.util.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LogPanel extends JPanel {
    private JTable logTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> logTypeComboBox;
    private JSpinner rowLimitSpinner;
    private JButton refreshButton;
    private JButton clearButton;

    public LogPanel() {
        setLayout(new BorderLayout());

        // Create toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel typeLabel = new JLabel("Log Type:");
        toolbarPanel.add(typeLabel);

        logTypeComboBox = new JComboBox<>(new String[]{"All Logs", "Errors Only", "Projects", "Tasks", "Resources"});
        toolbarPanel.add(logTypeComboBox);

        JLabel limitLabel = new JLabel("Row Limit:");
        toolbarPanel.add(limitLabel);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(100, 10, 1000, 10);
        rowLimitSpinner = new JSpinner(spinnerModel);
        toolbarPanel.add(rowLimitSpinner);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshLogs());
        toolbarPanel.add(refreshButton);

        clearButton = new JButton("Clear Logs");
        clearButton.addActionListener(e -> clearLogs());
        toolbarPanel.add(clearButton);

        add(toolbarPanel, BorderLayout.NORTH);

        // Create table
        String[] columnNames = {"Log ID", "Date/Time", "Operation", "Status", "Message", "User"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        logTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(logTable);
        add(scrollPane, BorderLayout.CENTER);

        // Load initial data
        refreshLogs();
    }

    private void refreshLogs() {
        tableModel.setRowCount(0);
        String logType = (String) logTypeComboBox.getSelectedItem();
        int rowLimit = (Integer) rowLimitSpinner.getValue();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = com.p6ebs.integration.util.DatabaseManager.getP6Connection();  // Assuming integration schema is in P6 database

            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT log_id, log_date, operation, status, message, user_id ")
                    .append("FROM p6_ebs_integration.integration_log ");

            // Add filter based on log type
            if (!"All Logs".equals(logType)) {
                sqlBuilder.append("WHERE ");
                if ("Errors Only".equals(logType)) {
                    sqlBuilder.append("status = 'ERROR' ");
                } else if ("Projects".equals(logType)) {
                    sqlBuilder.append("operation LIKE '%PROJECT%' ");
                } else if ("Tasks".equals(logType)) {
                    sqlBuilder.append("operation LIKE '%TASK%' ");
                } else if ("Resources".equals(logType)) {
                    sqlBuilder.append("operation LIKE '%RESOURCE%' ");
                }
            }

            sqlBuilder.append("ORDER BY log_date DESC ");
            sqlBuilder.append("FETCH FIRST ").append(rowLimit).append(" ROWS ONLY");

            stmt = conn.prepareStatement(sqlBuilder.toString());
            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getLong("log_id"),
                        rs.getTimestamp("log_date"),
                        rs.getString("operation"),
                        rs.getString("status"),
                        rs.getString("message"),
                        rs.getString("user_id")
                };
                tableModel.addRow(row);
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "No log entries found with the selected filter.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading logs: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            DatabaseManager.closeResources(rs, stmt, conn);
        }
    }

    private void clearLogs() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear all logs?\nThis action cannot be undone.",
                "Confirm Clear Logs",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = DatabaseManager.getP6Connection();  // Assuming integration schema is in P6 database

                String sql = "TRUNCATE TABLE p6_ebs_integration.integration_log";
                stmt = conn.prepareStatement(sql);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(
                        this,
                        "All logs have been cleared.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                refreshLogs();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Error clearing logs: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            } finally {
                DatabaseManager.closeResources(stmt, conn);
            }
        }
    }
}