package com.p6ebs.integration.ui;

import com.p6ebs.integration.dao.ProjectDAO;
import com.p6ebs.integration.dao.ResourceDAO;
import com.p6ebs.integration.model.Project;
import com.p6ebs.integration.model.Resource;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ResourcePanel extends JPanel {
    private JComboBox<String> viewTypeComboBox;
    private JComboBox<Project> projectComboBox;
    private JTable resourceTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton syncButton;
    private JButton syncAssignmentsButton;

    public ResourcePanel() {
        setLayout(new BorderLayout());

        // Create toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel viewLabel = new JLabel("View:");
        toolbarPanel.add(viewLabel);

        viewTypeComboBox = new JComboBox<>(new String[]{"Resources", "Resource Assignments", "Staging"});
        viewTypeComboBox.addActionListener(e -> {
            updateProjectVisibility();
            refreshResources();
        });
        toolbarPanel.add(viewTypeComboBox);

        JLabel projectLabel = new JLabel("Project:");
        toolbarPanel.add(projectLabel);

        projectComboBox = new JComboBox<>();
        projectComboBox.setPreferredSize(new Dimension(250, 25));
        projectComboBox.addActionListener(e -> refreshResources());
        toolbarPanel.add(projectComboBox);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshResources());
        toolbarPanel.add(refreshButton);

        syncButton = new JButton("Sync Resources");
        syncButton.addActionListener(e -> syncResources());
        toolbarPanel.add(syncButton);

        syncAssignmentsButton = new JButton("Sync Assignments");
        syncAssignmentsButton.addActionListener(e -> syncAssignments());
        toolbarPanel.add(syncAssignmentsButton);

        add(toolbarPanel, BorderLayout.NORTH);

        // Create table with default columns (will be updated based on view)
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resourceTable = new JTable(tableModel);
        resourceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(resourceTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add a status panel at the bottom
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        JLabel statusLabel = new JLabel("Ready");
        statusPanel.add(statusLabel, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);

        // Load projects
        loadProjects();

        // Initial setup
        updateProjectVisibility();
        updateTableColumns();
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
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            ResourcePanel.this,
                            "Error loading projects: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void updateProjectVisibility() {
        String viewType = (String) viewTypeComboBox.getSelectedItem();
        boolean showProject = "Resource Assignments".equals(viewType);

        projectComboBox.setVisible(showProject);
        Component[] components = ((JPanel)projectComboBox.getParent()).getComponents();
        for (Component c : components) {
            if (c instanceof JLabel && "Project:".equals(((JLabel)c).getText())) {
                c.setVisible(showProject);
                break;
            }
        }

        syncAssignmentsButton.setVisible(showProject);
        updateTableColumns();
    }

    private void updateTableColumns() {
        tableModel.setRowCount(0);
        String viewType = (String) viewTypeComboBox.getSelectedItem();

        if ("Resources".equals(viewType)) {
            tableModel.setColumnIdentifiers(new String[]{
                    "ID", "Name", "Short Name", "Type", "Email", "Source"
            });
        } else if ("Resource Assignments".equals(viewType)) {
            tableModel.setColumnIdentifiers(new String[]{
                    "Assignment ID", "Resource ID", "Resource Name", "Task ID", "Task Name",
                    "Planned Cost", "Actual Cost", "Remaining Cost"
            });
        } else if ("Staging".equals(viewType)) {
            tableModel.setColumnIdentifiers(new String[]{
                    "P6 ID", "Name", "Type", "EBS Org ID", "EBS Person ID", "Sync Status", "Error Message"
            });
        }
    }

    private void refreshResources() {
        String viewType = (String) viewTypeComboBox.getSelectedItem();
        Project selectedProject = (Project) projectComboBox.getSelectedItem();

        tableModel.setRowCount(0);

        SwingWorker<List<Resource>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Resource> doInBackground() throws Exception {
                ResourceDAO dao = new ResourceDAO();

                if ("Resources".equals(viewType)) {
                    return dao.getP6Resources();
                } else if ("Resource Assignments".equals(viewType) && selectedProject != null) {
                    return dao.getResourceAssignments(selectedProject.getId());
                } else if ("Staging".equals(viewType)) {
                    return dao.getStagingResources();
                } else {
                    return java.util.Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Resource> resources = get();
                    for (Resource resource : resources) {
                        Object[] row;

                        if ("Resources".equals(viewType)) {
                            row = new Object[]{
                                    resource.getId(),
                                    resource.getName(),
                                    resource.getShortName(),
                                    resource.getType(),
                                    resource.getEmail(),
                                    resource.getSource()
                            };
                        } else if ("Resource Assignments".equals(viewType)) {
                            row = new Object[]{
                                    resource.getAssignmentId(),
                                    resource.getId(),
                                    resource.getName(),
                                    resource.getTaskId(),
                                    resource.getTaskName(),
                                    resource.getPlannedCost(),
                                    resource.getActualCost(),
                                    resource.getRemainingCost()
                            };
                        } else { // Staging
                            row = new Object[]{
                                    resource.getId(),
                                    resource.getName(),
                                    resource.getType(),
                                    resource.getEbsOrgId(),
                                    resource.getEbsPersonId(),
                                    resource.getSyncStatus(),
                                    resource.getErrorMessage()
                            };
                        }

                        tableModel.addRow(row);
                    }

                    if (resources.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                ResourcePanel.this,
                                "No resources found for the selected view.",
                                "Information",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            ResourcePanel.this,
                            "Error loading resources: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void syncResources() {
        String viewType = (String) viewTypeComboBox.getSelectedItem();

        if ("Resources".equals(viewType)) {
            int selectedRow = resourceTable.getSelectedRow();
            if (selectedRow != -1) {
                Long resourceId = (Long) tableModel.getValueAt(selectedRow, 0);
                String resourceName = (String) tableModel.getValueAt(selectedRow, 1);

                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Synchronize resource: " + resourceName + "?",
                        "Confirm Sync",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    syncSpecificResource(resourceId);
                }
            } else {
                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Synchronize all resources?",
                        "Confirm Sync",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    syncAllResources();
                }
            }
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Please switch to Resources view to perform resource synchronization.",
                    "View Type Mismatch",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void syncSpecificResource(Long resourceId) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return new ResourceDAO().triggerResourceSync(resourceId);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                                ResourcePanel.this,
                                "Resource synchronization started successfully.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                                ResourcePanel.this,
                                "Failed to start resource synchronization.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            ResourcePanel.this,
                            "Error: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void syncAllResources() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return new ResourceDAO().triggerResourcesSync();
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                                ResourcePanel.this,
                                "Resources synchronization started successfully.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                                ResourcePanel.this,
                                "Failed to start resources synchronization.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            ResourcePanel.this,
                            "Error: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void syncAssignments() {
        String viewType = (String) viewTypeComboBox.getSelectedItem();

        if ("Resource Assignments".equals(viewType)) {
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
                    "Synchronize all resource assignments for project: " + selectedProject.getName() + "?",
                    "Confirm Sync",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return new ResourceDAO().triggerResourceAssignmentsSync(selectedProject.getId());
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(
                                        ResourcePanel.this,
                                        "Resource assignments synchronization started successfully.",
                                        "Success",
                                        JOptionPane.INFORMATION_MESSAGE
                                );
                            } else {
                                JOptionPane.showMessageDialog(
                                        ResourcePanel.this,
                                        "Failed to start resource assignments synchronization.",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE
                                );
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(
                                    ResourcePanel.this,
                                    "Error: " + e.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                };

                worker.execute();
            }
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Please switch to Resource Assignments view to perform assignments synchronization.",
                    "View Type Mismatch",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }
}