package com.p6ebs.integration.dao;

import com.p6ebs.integration.model.Task;
import com.p6ebs.integration.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    /**
     * Check if database is initialized
     */
    private void checkDatabaseConnection() throws SQLException {
        if (!DatabaseManager.isInitialized()) {
            throw new SQLException("Database connection not initialized. Please set up connection settings first.");
        }
    }

    /**
     * Get all tasks for a project from Primavera P6
     */
    public List<Task> getP6Tasks(long projectId) {
        List<Task> tasks = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();

            String sql = "SELECT task_id, task_code, task_name, parent_task_id, " +
                    "target_start_date, target_end_date, target_drtn_hr_cnt, " +
                    "phys_complete_pct, status_code " +
                    "FROM task " +
                    "WHERE proj_id = ? " +
                    "AND task_type <> 'WBS' " +
                    "AND delete_date IS NULL " +
                    "ORDER BY task_code";

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, projectId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getLong("task_id"));
                task.setCode(rs.getString("task_code"));
                task.setName(rs.getString("task_name"));
                task.setParentTaskId(rs.getLong("parent_task_id"));
                task.setStartDate(rs.getDate("target_start_date"));
                task.setEndDate(rs.getDate("target_end_date"));
                task.setDuration(rs.getDouble("target_drtn_hr_cnt") / 8.0); // Convert hours to days
                task.setPercentComplete(rs.getDouble("phys_complete_pct"));
                task.setStatus(rs.getString("status_code"));
                task.setProjectId(projectId);
                task.setSource("P6");
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Error getting P6 tasks: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeResources(rs, stmt, conn);
        }

        return tasks;
    }

    /**
     * Get all tasks for a project from Oracle EBS
     */
    public List<Task> getEBSTasks(long projectId) {
        List<Task> tasks = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getEBSConnection();

            String sql = "SELECT task_id, task_number, task_name, parent_task_id, " +
                    "start_date, finish_date, planned_duration, " +
                    "actual_percent_complete, task_status_code " +
                    "FROM pa_tasks " +
                    "WHERE project_id = ? " +
                    "ORDER BY task_number";

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, projectId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getLong("task_id"));
                task.setCode(rs.getString("task_number"));
                task.setName(rs.getString("task_name"));
                task.setParentTaskId(rs.getLong("parent_task_id"));
                task.setStartDate(rs.getDate("start_date"));
                task.setEndDate(rs.getDate("finish_date"));
                task.setDuration(rs.getDouble("planned_duration"));
                task.setPercentComplete(rs.getDouble("actual_percent_complete"));
                task.setStatus(rs.getString("task_status_code"));
                task.setProjectId(projectId);
                task.setSource("EBS");
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Error getting EBS tasks: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeResources(rs, stmt, conn);
        }

        return tasks;
    }

    /**
     * Get tasks from the staging table for a specific project
     */
    public List<Task> getStagingTasks(long projectId) {
        List<Task> tasks = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();  // Assuming integration schema is in P6 database

            String sql = "SELECT p6_task_id, p6_task_code, p6_task_name, " +
                    "p6_parent_task_id, p6_start_date, p6_finish_date, " +
                    "p6_duration, p6_percent_complete, ebs_task_id, " +
                    "sync_status, error_message, last_sync_date " +
                    "FROM p6_ebs_integration.task_staging " +
                    "WHERE p6_project_id = ? " +
                    "ORDER BY last_sync_date DESC";

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, projectId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getLong("p6_task_id"));
                task.setCode(rs.getString("p6_task_code"));
                task.setName(rs.getString("p6_task_name"));
                task.setParentTaskId(rs.getLong("p6_parent_task_id"));
                task.setStartDate(rs.getDate("p6_start_date"));
                task.setEndDate(rs.getDate("p6_finish_date"));
                task.setDuration(rs.getDouble("p6_duration"));
                task.setPercentComplete(rs.getDouble("p6_percent_complete"));
                task.setEbsId(rs.getLong("ebs_task_id"));
                task.setSyncStatus(rs.getString("sync_status"));
                task.setErrorMessage(rs.getString("error_message"));
                task.setLastSyncDate(rs.getTimestamp("last_sync_date"));
                task.setProjectId(projectId);
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Error getting staging tasks: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeResources(rs, stmt, conn);
        }

        return tasks;
    }

    /**
     * Trigger synchronization for all tasks in a project
     */
    public boolean triggerTasksSync(long projectId) {
        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();  // Assuming integration schema is in P6 database

            String sql = "{call p6_ebs_integration.task_sync.sync_tasks_p6_to_ebs(?)}";
            cstmt = conn.prepareCall(sql);
            cstmt.setLong(1, projectId);
            cstmt.execute();

            return true;
        } catch (SQLException e) {
            System.err.println("Error triggering tasks sync: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseManager.closeResources(cstmt, conn);
        }
    }

    /**
     * Trigger synchronization for a specific task
     */
    public boolean triggerTaskSync(long taskId) {
        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();  // Assuming integration schema is in P6 database

            String sql = "{call p6_ebs_integration.task_sync.sync_task(?)}";
            cstmt = conn.prepareCall(sql);
            cstmt.setLong(1, taskId);
            cstmt.execute();

            return true;
        } catch (SQLException e) {
            System.err.println("Error triggering task sync: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseManager.closeResources(cstmt, conn);
        }
    }

    /**
     * Trigger synchronization for task dependencies
     */
    public boolean triggerTaskDependenciesSync(long projectId) {
        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();  // Assuming integration schema is in P6 database

            String sql = "{call p6_ebs_integration.task_sync.sync_task_dependencies(?)}";
            cstmt = conn.prepareCall(sql);
            cstmt.setLong(1, projectId);
            cstmt.execute();

            return true;
        } catch (SQLException e) {
            System.err.println("Error triggering task dependencies sync: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseManager.closeResources(cstmt, conn);
        }
    }
}