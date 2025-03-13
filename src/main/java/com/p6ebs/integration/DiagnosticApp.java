package com.p6ebs.integration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DiagnosticApp {

    private static JTextArea logArea;
    private static int step = 0;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("P6-EBS Integration Diagnostic");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            JPanel mainPanel = new JPanel(new BorderLayout());

            // Control panel
            JPanel controlPanel = new JPanel();
            JButton nextButton = new JButton("Run Next Step");
            nextButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    runNextStep();
                }
            });
            controlPanel.add(nextButton);

            // Log area
            logArea = new JTextArea();
            logArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(logArea);

            mainPanel.add(controlPanel, BorderLayout.NORTH);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            frame.getContentPane().add(mainPanel);
            frame.setVisible(true);

            log("Diagnostic tool started. Click 'Run Next Step' to begin testing components.");
        });
    }

    private static void runNextStep() {
        step++;
        try {
            switch (step) {
                case 1:
                    testUIBasics();
                    break;
                case 2:
                    testConfigManager();
                    break;
                case 3:
                    testLoggerUtil();
                    break;
                case 4:
                    testDatabaseManagerInitialization();
                    break;
                case 5:
                    testDatabaseConnections();
                    break;
                case 6:
                    testDAOs();
                    break;
                case 7:
                    testDashboardPanel();
                    break;
                case 8:
                    testProjectPanel();
                    break;
                default:
                    log("All diagnostics complete!");
            }
        } catch (Throwable t) {
            log("ERROR in step " + step + ": " + t.getClass().getName() + ": " + t.getMessage());
            t.printStackTrace();
            StringBuffer stackTrace = new StringBuffer();
            for (StackTraceElement element : t.getStackTrace()) {
                stackTrace.append("    at " + element.toString() + "\n");
            }
            log(stackTrace.toString());
        }
    }

    private static void testUIBasics() {
        log("Step 1: Testing basic UI components");
        // Create and destroy a basic panel
        JPanel panel = new JPanel();
        panel.add(new JLabel("Test Label"));
        panel.add(new JButton("Test Button"));
        panel.add(new JTextField(20));
        log("Created and added basic components to panel");

        // Test layout managers
        panel.setLayout(new BorderLayout());
        panel.add(new JButton("North"), BorderLayout.NORTH);
        panel.add(new JButton("South"), BorderLayout.SOUTH);
        panel.add(new JButton("East"), BorderLayout.EAST);
        panel.add(new JButton("West"), BorderLayout.WEST);
        panel.add(new JButton("Center"), BorderLayout.CENTER);
        log("Tested BorderLayout");

        log("Basic UI test completed successfully");
    }

    private static void testConfigManager() {
        log("Step 2: Testing ConfigManager");
        try {
            log("Testing ConfigManager initialization");
            Class<?> configManagerClass = Class.forName("com.p6ebs.integration.util.ConfigManager");
            log("ConfigManager class loaded successfully");

            // Test getProperty method via reflection
            Object property = configManagerClass.getMethod("getProperty", String.class)
                    .invoke(null, "test.property");
            log("ConfigManager.getProperty() returned: " + property);

            log("ConfigManager test completed successfully");
        } catch (ClassNotFoundException e) {
            log("ConfigManager class not found. Make sure it's in the correct package.");
            throw new RuntimeException(e);
        } catch (Exception e) {
            log("Error testing ConfigManager: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void testLoggerUtil() {
        log("Step 3: Testing LoggerUtil");
        try {
            log("Testing LoggerUtil initialization");
            Class<?> loggerUtilClass = Class.forName("com.p6ebs.integration.util.LoggerUtil");
            log("LoggerUtil class loaded successfully");

            // Test getLogger method via reflection
            Object logger = loggerUtilClass.getMethod("getLogger", Class.class)
                    .invoke(null, DiagnosticApp.class);
            log("LoggerUtil.getLogger() returned: " + logger);

            log("LoggerUtil test completed successfully");
        } catch (ClassNotFoundException e) {
            log("LoggerUtil class not found. Make sure it's in the correct package.");
            throw new RuntimeException(e);
        } catch (Exception e) {
            log("Error testing LoggerUtil: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void testDatabaseManagerInitialization() {
        log("Step 4: Testing DatabaseManager initialization");
        try {
            log("Loading DatabaseManager class");
            Class.forName("com.p6ebs.integration.util.DatabaseManager");
            log("DatabaseManager class loaded successfully");
        } catch (ClassNotFoundException e) {
            log("DatabaseManager class not found. Make sure it's in the correct package.");
            throw new RuntimeException(e);
        } catch (Exception e) {
            log("Error loading DatabaseManager: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void testDatabaseConnections() {
        log("Step 5: Testing database connections");
        try {
            log("Testing P6 connection");
            Class<?> dbManagerClass = Class.forName("com.p6ebs.integration.util.DatabaseManager");
            Object p6Connection = dbManagerClass.getMethod("getP6Connection").invoke(null);
            log("P6 connection successful: " + p6Connection);

            log("Testing EBS connection");
            Object ebsConnection = dbManagerClass.getMethod("getEBSConnection").invoke(null);
            log("EBS connection successful: " + ebsConnection);

            log("Testing closeResources");
            dbManagerClass.getMethod("closeResources", AutoCloseable[].class)
                    .invoke(null, new Object[]{new AutoCloseable[]{(AutoCloseable)p6Connection, (AutoCloseable)ebsConnection}});
            log("Resources closed successfully");

            log("Database connections test completed successfully");
        } catch (Exception e) {
            log("Error testing database connections: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void testDAOs() {
        log("Step 6: Testing DAO classes");
        try {
            log("Testing ProjectDAO");
            Class<?> projectDAOClass = Class.forName("com.p6ebs.integration.dao.ProjectDAO");
            Object projectDAO = projectDAOClass.newInstance();
            Object p6Projects = projectDAOClass.getMethod("getP6Projects").invoke(projectDAO);
            log("ProjectDAO.getP6Projects() returned: " + p6Projects);

            log("Testing TaskDAO");
            Class<?> taskDAOClass = Class.forName("com.p6ebs.integration.dao.TaskDAO");
            Object taskDAO = taskDAOClass.newInstance();
            // We'll pass a dummy project ID
            Object tasks = taskDAOClass.getMethod("getP6Tasks", long.class).invoke(taskDAO, 1L);
            log("TaskDAO.getP6Tasks() returned: " + tasks);

            log("Testing ResourceDAO");
            Class<?> resourceDAOClass = Class.forName("com.p6ebs.integration.dao.ResourceDAO");
            Object resourceDAO = resourceDAOClass.newInstance();
            Object resources = resourceDAOClass.getMethod("getP6Resources").invoke(resourceDAO);
            log("ResourceDAO.getP6Resources() returned: " + resources);

            log("DAO classes test completed successfully");
        } catch (Exception e) {
            log("Error testing DAO classes: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void testDashboardPanel() {
        log("Step 7: Testing DashboardPanel");
        try {
            log("Creating DashboardPanel instance");
            Class<?> dashboardPanelClass = Class.forName("com.p6ebs.integration.ui.DashboardPanel");
            Object dashboardPanel = dashboardPanelClass.newInstance();
            log("DashboardPanel created successfully: " + dashboardPanel);

            log("DashboardPanel test completed successfully");
        } catch (Exception e) {
            log("Error testing DashboardPanel: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void testProjectPanel() {
        log("Step 8: Testing ProjectPanel");
        try {
            log("Creating ProjectPanel instance");
            Class<?> projectPanelClass = Class.forName("com.p6ebs.integration.ui.ProjectPanel");
            Object projectPanel = projectPanelClass.newInstance();
            log("ProjectPanel created successfully: " + projectPanel);

            log("ProjectPanel test completed successfully");
        } catch (Exception e) {
            log("Error testing ProjectPanel: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void log(String message) {
        logArea.append(message + "\n");
        // Auto-scroll to bottom
        logArea.setCaretPosition(logArea.getText().length());
        // Also print to console
        System.out.println(message);
    }
}