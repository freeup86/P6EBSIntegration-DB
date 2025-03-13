package com.p6ebs.integration.model;

import java.util.Date;

/**
 * Represents a task from either P6 or EBS
 */
public class Task {
    private Long id;
    private String code;
    private String name;
    private Long parentTaskId;
    private Date startDate;
    private Date endDate;
    private Double duration;
    private Double percentComplete;
    private String status;
    private Long projectId;
    private String source;
    private Long ebsId;
    private String syncStatus;
    private String errorMessage;
    private Date lastSyncDate;

    // Constructors
    public Task() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getParentTaskId() { return parentTaskId; }
    public void setParentTaskId(Long parentTaskId) { this.parentTaskId = parentTaskId; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public Double getDuration() { return duration; }
    public void setDuration(Double duration) { this.duration = duration; }

    public Double getPercentComplete() { return percentComplete; }
    public void setPercentComplete(Double percentComplete) { this.percentComplete = percentComplete; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Long getEbsId() { return ebsId; }
    public void setEbsId(Long ebsId) { this.ebsId = ebsId; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Date getLastSyncDate() { return lastSyncDate; }
    public void setLastSyncDate(Date lastSyncDate) { this.lastSyncDate = lastSyncDate; }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}