package com.p6ebs.integration.dao;

import com.p6ebs.integration.model.Project;
import com.p6ebs.integration.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    /**
     * Check if database is initialized
     */
    private void checkDatabaseConnection() throws SQLException {
        if (!DatabaseManager.isInitialized()) {
            throw new SQLException("Database connection not initialized. Please set up connection settings first.");
        }
    }

    /**
     * Get all projects from Primavera P6
     */
    public List<Project> getP6Projects() {
        List<Project> projects = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();

            String sql = "SELECT proj.proj_id, wb.proj_short_name, proj.anticipated_start_date, " +
                    "proj.anticipated_finish_date, proj.status_code " +
                    "FROM project proj " +
                    "JOIN projwbs wb ON proj.proj_id = wb.proj_id " +
                    "WHERE wb.proj_node_flag = 'Y' " +
                    "AND proj.delete_date IS NULL " +
                    "ORDER BY wb.proj_short_name";

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Project project = new Project();
                project.setId(rs.getLong("proj_id"));
                project.setName(rs.getString("proj_short_name"));
                project.setStartDate(rs.getDate("anticipated_start_date"));
                project.setEndDate(rs.getDate("anticipated_finish_date"));
                project.setStatus(rs.getString("status_code"));
                project.setSource("P6");
                projects.add(project);
            }
        } catch (SQLException e) {
            System.err.println("Error getting P6 projects: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeResources(rs, stmt, conn);
        }

        return projects;
    }

    /**
     * Get all projects from Oracle EBS
     */
    public List<Project> getEBSProjects() {
        List<Project> projects = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getEBSConnection();

            String sql = "SELECT project_id, name, description, start_date, " +
                    "completion_date, project_status_code " +
                    "FROM pa_projects_all " +
                    "WHERE template_flag = 'N' " +
                    "AND enabled_flag = 'Y' " +
                    "ORDER BY name";

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Project project = new Project();
                project.setId(rs.getLong("project_id"));
                project.setName(rs.getString("name"));
                project.setDescription(rs.getString("description"));
                project.setStartDate(rs.getDate("start_date"));
                project.setEndDate(rs.getDate("completion_date"));
                project.setStatus(rs.getString("project_status_code"));
                project.setSource("EBS");
                projects.add(project);
            }
        } catch (SQLException e) {
            System.err.println("Error getting EBS projects: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeResources(rs, stmt, conn);
        }

        return projects;
    }

    /**
     * Get projects from the staging table
     */
    public List<Project> getStagingProjects() {
        List<Project> projects = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();  // Assuming integration schema is in P6 database

            String sql = "SELECT p6_project_id, p6_project_name, p6_start_date, " +
                    "p6_finish_date, ebs_project_id, sync_status, error_message, last_sync_date " +
                    "FROM p6_ebs_integration.project_staging " +
                    "ORDER BY last_sync_date DESC";

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Project project = new Project();
                project.setId(rs.getLong("p6_project_id"));
                project.setName(rs.getString("p6_project_name"));
                project.setStartDate(rs.getDate("p6_start_date"));
                project.setEndDate(rs.getDate("p6_finish_date"));
                project.setSyncStatus(rs.getString("sync_status"));
                project.setErrorMessage(rs.getString("error_message"));
                project.setLastSyncDate(rs.getTimestamp("last_sync_date"));
                projects.add(project);
            }
        } catch (SQLException e) {
            System.err.println("Error getting staging projects: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeResources(rs, stmt, conn);
        }

        return projects;
    }

    /**
     * Trigger synchronization for a specific project
     */
    public boolean triggerProjectSync(long projectId) {
        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();  // Assuming integration schema is in P6 database

            String sql = "{call p6_ebs_integration.project_sync.sync_project(?)}";
            cstmt = conn.prepareCall(sql);
            cstmt.setLong(1, projectId);
            cstmt.execute();

            return true;
        } catch (SQLException e) {
            System.err.println("Error triggering project sync: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseManager.closeResources(cstmt, conn);
        }
    }

    /**
     * Trigger synchronization for all projects
     */
    public boolean triggerAllProjectsSync() {
        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();  // Assuming integration schema is in P6 database

            String sql = "{call p6_ebs_integration.project_sync.sync_projects_p6_to_ebs}";
            cstmt = conn.prepareCall(sql);
            cstmt.execute();

            return true;
        } catch (SQLException e) {
            System.err.println("Error triggering all projects sync: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseManager.closeResources(cstmt, conn);
        }
    }

    /**
     * Trigger reverse synchronization (EBS to P6)
     */
    public boolean triggerReverseProjectSync() {
        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();  // Assuming integration schema is in P6 database

            String sql = "{call p6_ebs_integration.project_sync.sync_projects_ebs_to_p6}";
            cstmt = conn.prepareCall(sql);
            cstmt.execute();

            return true;
        } catch (SQLException e) {
            System.err.println("Error triggering reverse project sync: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseManager.closeResources(cstmt, conn);
        }
    }
}