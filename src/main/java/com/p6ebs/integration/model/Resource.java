package com.p6ebs.integration.model;

import java.util.Date;

/**
 * Represents a resource from either P6 or EBS
 */
public class Resource {
    private Long id;
    private String name;
    private String shortName;
    private String type;
    private String email;
    private Long parentId;
    private boolean internal;
    private Date startDate;
    private Date endDate;
    private String source;

    // For resource assignments
    private Long assignmentId;
    private Long taskId;
    private String taskName;
    private Double plannedCost;
    private Double actualCost;
    private Double remainingCost;
    private Double plannedQuantity;
    private Double actualQuantity;
    private Double remainingQuantity;

    // For synchronization
    private Long ebsOrgId;
    private Long ebsPersonId;
    private String syncStatus;
    private String errorMessage;
    private Date lastSyncDate;

    // Constructors
    public Resource() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public boolean isInternal() { return internal; }
    public void setInternal(boolean internal) { this.internal = internal; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public Double getPlannedCost() { return plannedCost; }
    public void setPlannedCost(Double plannedCost) { this.plannedCost = plannedCost; }

    public Double getActualCost() { return actualCost; }
    public void setActualCost(Double actualCost) { this.actualCost = actualCost; }

    public Double getRemainingCost() { return remainingCost; }
    public void setRemainingCost(Double remainingCost) { this.remainingCost = remainingCost; }

    public Double getPlannedQuantity() { return plannedQuantity; }
    public void setPlannedQuantity(Double plannedQuantity) { this.plannedQuantity = plannedQuantity; }

    public Double getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(Double actualQuantity) { this.actualQuantity = actualQuantity; }

    public Double getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(Double remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public Long getEbsOrgId() { return ebsOrgId; }
    public void setEbsOrgId(Long ebsOrgId) { this.ebsOrgId = ebsOrgId; }

    public Long getEbsPersonId() { return ebsPersonId; }
    public void setEbsPersonId(Long ebsPersonId) { this.ebsPersonId = ebsPersonId; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Date getLastSyncDate() { return lastSyncDate; }
    public void setLastSyncDate(Date lastSyncDate) { this.lastSyncDate = lastSyncDate; }

    @Override
    public String toString() {
        return name + (shortName != null ? " (" + shortName + ")" : "");
    }
}