package com.p6ebs.integration.model;

public class ConnectionSettings {
    private String p6Url;
    private String p6Username;
    private String p6Password;
    private String ebsUrl;
    private String ebsUsername;
    private String ebsPassword;
    private String integrationSchema;

    // Default constructor
    public ConnectionSettings() {
        // Default values
        this.p6Url = "jdbc:oracle:thin:@//localhost:1521/orcl";
        this.p6Username = "p6_user";
        this.p6Password = "";
        this.ebsUrl = "jdbc:oracle:thin:@//localhost:1521/orcl";
        this.ebsUsername = "ebs_user";
        this.ebsPassword = "";
        this.integrationSchema = "p6_ebs_integration";
    }

    // Getters and setters
    public String getP6Url() { return p6Url; }
    public void setP6Url(String p6Url) { this.p6Url = p6Url; }

    public String getP6Username() { return p6Username; }
    public void setP6Username(String p6Username) { this.p6Username = p6Username; }

    public String getP6Password() { return p6Password; }
    public void setP6Password(String p6Password) { this.p6Password = p6Password; }

    public String getEbsUrl() { return ebsUrl; }
    public void setEbsUrl(String ebsUrl) { this.ebsUrl = ebsUrl; }

    public String getEbsUsername() { return ebsUsername; }
    public void setEbsUsername(String ebsUsername) { this.ebsUsername = ebsUsername; }

    public String getEbsPassword() { return ebsPassword; }
    public void setEbsPassword(String ebsPassword) { this.ebsPassword = ebsPassword; }

    public String getIntegrationSchema() { return integrationSchema; }
    public void setIntegrationSchema(String integrationSchema) { this.integrationSchema = integrationSchema; }
}