package com.example.incidents.dto;

import com.example.incidents.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateIncidentRequest {

    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "severity is required")
    private Severity severity;

    @NotBlank(message = "reportedBy is required")
    @Size(max = 120, message = "reportedBy must not exceed 120 characters")
    private String reportedBy;

    @Size(max = 120, message = "externalReferenceId must not exceed 120 characters")
    private String externalReferenceId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public String getExternalReferenceId() { return externalReferenceId; }
    public void setExternalReferenceId(String externalReferenceId) { this.externalReferenceId = externalReferenceId; }
}
