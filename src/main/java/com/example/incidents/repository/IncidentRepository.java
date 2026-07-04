package com.example.incidents.repository;

import com.example.incidents.entity.Incident;
import com.example.incidents.enums.IncidentStatus;
import com.example.incidents.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    Optional<Incident> findByExternalReferenceId(String externalReferenceId);
    List<Incident> findBySeverity(Severity severity);
    List<Incident> findByStatus(IncidentStatus status);
    List<Incident> findBySeverityAndStatus(Severity severity, IncidentStatus status);
}
