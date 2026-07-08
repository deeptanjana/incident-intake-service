package com.example.incidents.dto;

import com.example.incidents.entity.Incident;

public record CreateIncidentApiResponse(
        boolean created,
        String message,
        Incident incident
) {
}
