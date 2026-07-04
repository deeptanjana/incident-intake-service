package com.example.incidents.dto;

import com.example.incidents.entity.Incident;

public record CreateIncidentResult(Incident incident, boolean created) {
}
