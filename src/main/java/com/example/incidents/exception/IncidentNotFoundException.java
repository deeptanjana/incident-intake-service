package com.example.incidents.exception;

import java.util.UUID;

public class IncidentNotFoundException extends RuntimeException {

    public IncidentNotFoundException(UUID incidentId) {
        super("Incident not found: " + incidentId);
    }
}
