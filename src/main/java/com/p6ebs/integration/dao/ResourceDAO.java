package com.p6ebs.integration.dao;

import com.p6ebs.integration.model.Resource;
import com.p6ebs.integration.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {

    /**
     * Check if database is initialized
     */
    private void checkDatabaseConnection() throws SQLException {
        if (!DatabaseManager.isInitialized()) {
            throw new SQLException("Database connection not initialized. Please set up connection settings first.");
        }
    }

    /**
     * Get all resources from Primavera P6
     */
    public List<Resource> getP6Resources() {
        List<Resource> resources = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();

            String sql = "SELECT rsrc_id, rsrc_name, rsrc_short_name, " +
                    "rsrc_type, email_addr, parent_rsrc_id " +
                    "FROM rsrc " +
                    "WHERE delete_date IS NULL " +
                    "ORDER BY rsrc_name";

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Resource resource = new Resource();
                resource.setId(rs.getLong("rsrc_id"));
                resource.setName(rs.getString("rsrc_name"));
                resource.setShortName(rs.getString("rsrc_short_name"));
                resource.setType(rs.getString("rsrc_type"));
                resource.setEmail(rs.getString("email_addr"));
                resource.setParentId(rs.getLong("parent_rsrc_id"));
                resource.setSource("P6");
                resources.add(resource);
            }
        } catch (SQLException e) {
            System.err.println("Error getting P6 resources: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeResources(rs, stmt, conn);
        }

        return resources;
    }

    /**
     * Get all resources from Oracle EBS
     */
    public List<Resource> getEBSResources() {
        List<Resource> resources = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getEBSConnection();

            String sql = "SELECT organization_id, name, type, " +
                    "internal_external_flag, date_from, date_to " +
                    "FROM hr_all_organization_units " +
                    "WHERE date_to IS NULL OR date_to > SYSDATE " +
                    "ORDER BY name";

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Resource resource = new Resource();
                resource.setId(rs.getLong("organization_id"));
                resource.setName(rs.getString("name"));
                resource.setType(rs.getString("type"));
                resource.setInternal("I".equals(rs.getString("internal_external_flag")));
                resource.setStartDate(rs.getDate("date_from"));
                resource.setEndDate(rs.getDate("date_to"));
                resource.setSource("EBS");
                resources.add(resource);
            }
        } catch (SQLException e) {
            System.err.println("Error getting EBS resources: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeResources(rs, stmt, conn);
        }

        return resources;
    }

    /**
     * Get resources from the staging table
     */
    public List<Resource> getStagingResources() {
        List<Resource> resources = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();  // Assuming integration schema is in P6 database

            String sql = "SELECT p6_resource_id, p6_resource_name, p6_resource_short_name, " +
                    "p6_resource_type, p6_email, ebs_organization_id, ebs_person_id, " +
                    "sync_status, error_message, last_sync_date " +
                    "FROM p6_ebs_integration.resource_staging " +
                    "ORDER BY last_sync_date DESC";

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Resource resource = new Resource();
                resource.setId(rs.getLong("p6_resource_id"));
                resource.setName(rs.getString("p6_resource_name"));
                resource.setShortName(rs.getString("p6_resource_short_name"));
                resource.setType(rs.getString("p6_resource_type"));
                resource.setEmail(rs.getString("p6_email"));
                resource.setEbsOrgId(rs.getLong("ebs_organization_id"));
                resource.setEbsPersonId(rs.getLong("ebs_person_id"));
                resource.setSyncStatus(rs.getString("sync_status"));
                resource.setErrorMessage(rs.getString("error_message"));
                resource.setLastSyncDate(rs.getTimestamp("last_sync_date"));
                resources.add(resource);
            }
        } catch (SQLException e) {
            System.err.println("Error getting staging resources: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeResources(rs, stmt, conn);
        }

        return resources;
    }

    /**
     * Get resource assignments for a project
     */
    public List<Resource> getResourceAssignments(long projectId) {
        List<Resource> resources = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();

            String sql = "SELECT tr.taskrsrc_id, tr.task_id, tr.rsrc_id, " +
                    "t.task_name, r.rsrc_name, " +
                    "tr.target_cost, tr.act_cost, tr.remain_cost, " +
                    "tr.target_qty, tr.act_qty, tr.remain_qty " +
                    "FROM taskrsrc tr " +
                    "JOIN task t ON tr.task_id = t.task_id " +
                    "JOIN rsrc r ON tr.rsrc_id = r.rsrc_id " +
                    "WHERE t.proj_id = ? " +
                    "AND tr.delete_date IS NULL " +
                    "ORDER BY t.task_name, r.rsrc_name";

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, projectId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Resource resource = new Resource();
                resource.setId(rs.getLong("rsrc_id"));
                resource.setName(rs.getString("rsrc_name"));
                resource.setAssignmentId(rs.getLong("taskrsrc_id"));
                resource.setTaskId(rs.getLong("task_id"));
                resource.setTaskName(rs.getString("task_name"));
                resource.setPlannedCost(rs.getDouble("target_cost"));
                resource.setActualCost(rs.getDouble("act_cost"));
                resource.setRemainingCost(rs.getDouble("remain_cost"));
                resource.setPlannedQuantity(rs.getDouble("target_qty"));
                resource.setActualQuantity(rs.getDouble("act_qty"));
                resource.setRemainingQuantity(rs.getDouble("remain_qty"));
                resource.setSource("P6");
                resources.add(resource);
            }
        } catch (SQLException e) {
            System.err.println("Error getting resource assignments: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeResources(rs, stmt, conn);
        }

        return resources;
    }

    /**
     * Trigger synchronization for all resources
     */
    public boolean triggerResourcesSync() {
        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();  // Assuming integration schema is in P6 database

            String sql = "{call p6_ebs_integration.resource_sync.sync_resources}";
            cstmt = conn.prepareCall(sql);
            cstmt.execute();

            return true;
        } catch (SQLException e) {
            System.err.println("Error triggering resources sync: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseManager.closeResources(cstmt, conn);
        }
    }

    /**
     * Trigger synchronization for a specific resource
     */
    public boolean triggerResourceSync(long resourceId) {
        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();  // Assuming integration schema is in P6 database

            String sql = "{call p6_ebs_integration.resource_sync.sync_resource(?)}";
            cstmt = conn.prepareCall(sql);
            cstmt.setLong(1, resourceId);
            cstmt.execute();

            return true;
        } catch (SQLException e) {
            System.err.println("Error triggering resource sync: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseManager.closeResources(cstmt, conn);
        }
    }

    /**
     * Trigger synchronization for resource assignments in a project
     */
    public boolean triggerResourceAssignmentsSync(long projectId) {
        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            checkDatabaseConnection();

            conn = DatabaseManager.getP6Connection();  // Assuming integration schema is in P6 database

            String sql = "{call p6_ebs_integration.resource_sync.sync_resource_assignments(?)}";
            cstmt = conn.prepareCall(sql);
            cstmt.setLong(1, projectId);
            cstmt.execute();

            return true;
        } catch (SQLException e) {
            System.err.println("Error triggering resource assignments sync: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseManager.closeResources(cstmt, conn);
        }
    }
}