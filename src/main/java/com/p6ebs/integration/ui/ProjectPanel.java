package com.p6ebs.integration.ui;

import com.p6ebs.integration.dao.ProjectDAO;
import com.p6ebs.integration.model.Project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProjectPanel extends JPanel {
    private JTable projectTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> sourceComboBox;
    private JButton refreshButton;
    private JButton syncButton;

    public ProjectPanel() {
        setLayout(new BorderLayout());

        // Create toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel sourceLabel = new JLabel("Source:");
        toolbarPanel.add(sourceLabel);

        sourceComboBox = new JComboBox<>(new String[]{"P6", "EBS", "Staging"});
        sourceComboBox.addActionListener(e -> refreshProjects());
        toolbarPanel.add(sourceComboBox);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshProjects());
        toolbarPanel.add(refreshButton);

        syncButton = new JButton("Sync Selected");
        syncButton.addActionListener(e -> syncSelectedProject());
        toolbarPanel.add(syncButton);

        add(toolbarPanel, BorderLayout.NORTH);

        // Create table
        String[] columnNames = {"ID", "Name", "Start Date", "End Date", "Status", "Source"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        projectTable = new JTable(tableModel);
        projectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(projectTable);
        add(scrollPane, BorderLayout.CENTER);

        // Initial data load
        refreshProjects();
    }

    private void refreshProjects() {
        tableModel.setRowCount(0);

        SwingWorker<List<Project>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Project> doInBackground() throws Exception {
                ProjectDAO dao = new ProjectDAO();
                String source = (String) sourceComboBox.getSelectedItem();

                if ("P6".equals(source)) {
                    return dao.getP6Projects();
                } else if ("EBS".equals(source)) {
                    return dao.getEBSProjects();
                } else {
                    return dao.getStagingProjects();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Project> projects = get();
                    for (Project project : projects) {
                        Object[] row = {
                                project.getId(),
                                project.getName(),
                                project.getStartDate(),
                                project.getEndDate(),
                                project.getStatus(),
                                project.getSource()
                        };
                        tableModel.addRow(row);
                    }

                    if (projects.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                ProjectPanel.this,
                                "No projects found.",
                                "Information",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            ProjectPanel.this,
                            "Error loading projects: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void syncSelectedProject() {
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a project to synchronize.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Long projectId = (Long) tableModel.getValueAt(selectedRow, 0);
        String projectName = (String) tableModel.getValueAt(selectedRow, 1);

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Synchronize project: " + projectName + "?",
                "Confirm Sync",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return new ProjectDAO().triggerProjectSync(projectId);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(
                                    ProjectPanel.this,
                                    "Project synchronization started successfully.",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            JOptionPane.showMessageDialog(
                                    ProjectPanel.this,
                                    "Failed to start project synchronization.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(
                                ProjectPanel.this,
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