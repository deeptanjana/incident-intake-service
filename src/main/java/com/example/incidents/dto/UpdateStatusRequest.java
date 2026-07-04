package com.example.incidents.dto;

import com.example.incidents.enums.IncidentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateStatusRequest {

    @NotNull(message = "status is required")
    private IncidentStatus status;

    @NotBlank(message = "updatedBy is required")
    @Size(max = 120, message = "updatedBy must not exceed 120 characters")
    private String updatedBy;

    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
