package com.example.incidents.dto;

import com.example.incidents.entity.Incident;
import com.example.incidents.enums.IncidentStatus;
import com.example.incidents.enums.Severity;

import java.time.LocalDateTime;
import java.util.UUID;

public record IncidentResponse(
        UUID id,
        String title,
        String description,
        Severity severity,
        IncidentStatus status,
        String reportedBy,
        String externalReferenceId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getReportedBy(),
                incident.getExternalReferenceId(),
                incident.getCreatedAt(),
                incident.getUpdatedAt()
        );
    }
}
