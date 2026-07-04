package com.example.incidents.dto;

import com.example.incidents.entity.IncidentEvent;
import com.example.incidents.enums.IncidentEventType;

import java.time.LocalDateTime;
import java.util.UUID;

public record IncidentEventResponse(
        UUID id,
        UUID incidentId,
        IncidentEventType eventType,
        String oldValue,
        String newValue,
        String message,
        String createdBy,
        LocalDateTime createdAt
) {
    public static IncidentEventResponse from(IncidentEvent event) {
        return new IncidentEventResponse(
                event.getId(),
                event.getIncidentId(),
                event.getEventType(),
                event.getOldValue(),
                event.getNewValue(),
                event.getMessage(),
                event.getCreatedBy(),
                event.getCreatedAt()
        );
    }
}
