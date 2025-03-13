package com.p6ebs.integration.ui;

import com.p6ebs.integration.dao.ProjectDAO;
import com.p6ebs.integration.dao.TaskDAO;
import com.p6ebs.integration.model.Project;
import com.p6ebs.integration.model.Task;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TaskPanel extends JPanel {
    private JComboBox<Project> projectComboBox;
    private JComboBox<String> sourceComboBox;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton syncButton;
    private JButton syncDependenciesButton;

    public TaskPanel() {
        setLayout(new BorderLayout());

        // Create toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel projectLabel = new JLabel("Project:");
        toolbarPanel.add(projectLabel);

        projectComboBox = new JComboBox<>();
        projectComboBox.setPreferredSize(new Dimension(250, 25));
        projectComboBox.addActionListener(e -> refreshTasks());
        toolbarPanel.add(projectComboBox);

        JLabel sourceLabel = new JLabel("Source:");
        toolbarPanel.add(sourceLabel);

        sourceComboBox = new JComboBox<>(new String[]{"P6", "EBS", "Staging"});
        sourceComboBox.addActionListener(e -> refreshTasks());
        toolbarPanel.add(sourceComboBox);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshTasks());
        toolbarPanel.add(refreshButton);

        syncButton = new JButton("Sync Selected Task");
        syncButton.addActionListener(e -> syncSelectedTask());
        toolbarPanel.add(syncButton);

        syncDependenciesButton = new JButton("Sync Dependencies");
        syncDependenciesButton.addActionListener(e -> syncDependencies());
        toolbarPanel.add(syncDependenciesButton);

        add(toolbarPanel, BorderLayout.NORTH);

        // Create table
        String[] columnNames = {"ID", "Code", "Name", "Start Date", "End Date", "Duration", "% Complete", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        taskTable = new JTable(tableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(taskTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add a status panel at the bottom
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        JLabel statusLabel = new JLabel("Ready");
        statusPanel.add(statusLabel, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);

        // Load projects
        loadProjects();
    }

    private void loadProjects() {
        projectComboBox.removeAllItems();

        SwingWorker<List<Project>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Project> doInBackground() throws Exception {
                return new ProjectDAO().getP6Projects();
            }

            @Override
            protected void done() {
                try {
                    List<Project> projects = get();
                    for (Project project : projects) {
                        projectComboBox.addItem(project);
                    }

                    if (projects.size() > 0) {
                        projectComboBox.setSelectedIndex(0);
                        refreshTasks();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            TaskPanel.this,
                            "Error loading projects: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void refreshTasks() {
        Project selectedProject = (Project) projectComboBox.getSelectedItem();
        if (selectedProject == null) {
            return;
        }

        tableModel.setRowCount(0);

        SwingWorker<List<Task>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Task> doInBackground() throws Exception {
                TaskDAO dao = new TaskDAO();
                String source = (String) sourceComboBox.getSelectedItem();

                if ("P6".equals(source)) {
                    return dao.getP6Tasks(selectedProject.getId());
                } else if ("EBS".equals(source)) {
                    return dao.getEBSTasks(selectedProject.getId());
                } else {
                    return dao.getStagingTasks(selectedProject.getId());
                }
            }

            @Override
            protected void done() {
                try {
                    List<Task> tasks = get();
                    for (Task task : tasks) {
                        Object[] row = {
                                task.getId(),
                                task.getCode(),
                                task.getName(),
                                task.getStartDate(),
                                task.getEndDate(),
                                task.getDuration(),
                                task.getPercentComplete(),
                                task.getStatus()
                        };
                        tableModel.addRow(row);
                    }

                    if (tasks.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                TaskPanel.this,
                                "No tasks found for the selected project.",
                                "Information",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            TaskPanel.this,
                            "Error loading tasks: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void syncSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a task to synchronize.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Long taskId = (Long) tableModel.getValueAt(selectedRow, 0);
        String taskName = (String) tableModel.getValueAt(selectedRow, 2);

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Synchronize task: " + taskName + "?",
                "Confirm Sync",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return new TaskDAO().triggerTaskSync(taskId);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(
                                    TaskPanel.this,
                                    "Task synchronization started successfully.",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            JOptionPane.showMessageDialog(
                                    TaskPanel.this,
                                    "Failed to start task synchronization.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(
                                TaskPanel.this,
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

    private void syncDependencies() {
        Project selectedProject = (Project) projectComboBox.getSelectedItem();
        if (selectedProject == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a project first.",
                    "No Project Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Synchronize all task dependencies for project: " + selectedProject.getName() + "?",
                "Confirm Sync",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return new TaskDAO().triggerTaskDependenciesSync(selectedProject.getId());
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(
                                    TaskPanel.this,
                                    "Task dependencies synchronization started successfully.",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            JOptionPane.showMessageDialog(
                                    TaskPanel.this,
                                    "Failed to start task dependencies synchronization.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(
                                TaskPanel.this,
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