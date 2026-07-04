package com.example.incidents.service;

import com.example.incidents.dto.CreateIncidentRequest;
import com.example.incidents.dto.CreateIncidentResult;
import com.example.incidents.dto.UpdateStatusRequest;
import com.example.incidents.entity.Incident;
import com.example.incidents.entity.IncidentEvent;
import com.example.incidents.enums.IncidentEventType;
import com.example.incidents.enums.IncidentStatus;
import com.example.incidents.enums.Severity;
import com.example.incidents.exception.IncidentNotFoundException;
import com.example.incidents.repository.IncidentEventRepository;
import com.example.incidents.repository.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IncidentService {

    private static final Logger log = LoggerFactory.getLogger(IncidentService.class);

    private final IncidentRepository incidentRepository;
    private final IncidentEventRepository incidentEventRepository;

    public IncidentService(IncidentRepository incidentRepository, IncidentEventRepository incidentEventRepository) {
        this.incidentRepository = incidentRepository;
        this.incidentEventRepository = incidentEventRepository;
    }

    @Transactional
    public CreateIncidentResult createIncident(CreateIncidentRequest request) {
        String externalReferenceId = normalize(request.getExternalReferenceId());

        if (externalReferenceId != null) {
            Optional<Incident> existing = incidentRepository.findByExternalReferenceId(externalReferenceId);
            if (existing.isPresent()) {
                log.info("event=incident.create.idempotent_duplicate_returned externalReferenceId={} incidentId={} reportedBy={}",
                        externalReferenceId, existing.get().getId(), request.getReportedBy());
                return new CreateIncidentResult(existing.get(), false);
            }
        }

        Incident incident = new Incident();
        incident.setTitle(request.getTitle().trim());
        incident.setDescription(normalize(request.getDescription()));
        incident.setSeverity(request.getSeverity());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setReportedBy(request.getReportedBy().trim());
        incident.setExternalReferenceId(externalReferenceId);

        Incident saved = incidentRepository.save(incident);
        saveEvent(saved.getId(), IncidentEventType.INCIDENT_CREATED, null, saved.getStatus().name(),
                "Incident created", saved.getReportedBy());

        log.info("event=incident.create.created incidentId={} severity={} status={} reportedBy={} externalReferenceId={}",
                saved.getId(), saved.getSeverity(), saved.getStatus(), saved.getReportedBy(), saved.getExternalReferenceId());

        return new CreateIncidentResult(saved, true);
    }

    @Transactional(readOnly = true)
    public Incident getIncident(UUID id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Incident> listIncidents(Severity severity, IncidentStatus status) {
        if (severity != null && status != null) {
            return incidentRepository.findBySeverityAndStatus(severity, status);
        }
        if (severity != null) {
            return incidentRepository.findBySeverity(severity);
        }
        if (status != null) {
            return incidentRepository.findByStatus(status);
        }
        return incidentRepository.findAll();
    }

    @Transactional
    public Incident updateStatus(UUID id, UpdateStatusRequest request) {
        Incident incident = getIncident(id);
        IncidentStatus oldStatus = incident.getStatus();
        IncidentStatus newStatus = request.getStatus();

        if (oldStatus == newStatus) {
            log.info("event=incident.status.no_change incidentId={} status={} updatedBy={}",
                    incident.getId(), oldStatus, request.getUpdatedBy());
            return incident;
        }

        incident.changeStatus(newStatus);
        Incident saved = incidentRepository.save(incident);

        saveEvent(saved.getId(), IncidentEventType.STATUS_CHANGED, oldStatus.name(), newStatus.name(),
                "Incident status changed from " + oldStatus + " to " + newStatus, request.getUpdatedBy().trim());

        log.info("event=incident.status.changed incidentId={} oldStatus={} newStatus={} updatedBy={}",
                saved.getId(), oldStatus, newStatus, request.getUpdatedBy());

        return saved;
    }

    @Transactional(readOnly = true)
    public List<IncidentEvent> getIncidentHistory(UUID incidentId) {
        getIncident(incidentId);
        return incidentEventRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId);
    }

    private void saveEvent(UUID incidentId, IncidentEventType eventType, String oldValue,
                           String newValue, String message, String createdBy) {
        IncidentEvent event = new IncidentEvent();
        event.setIncidentId(incidentId);
        event.setEventType(eventType);
        event.setOldValue(oldValue);
        event.setNewValue(newValue);
        event.setMessage(message);
        event.setCreatedBy(createdBy);
        incidentEventRepository.save(event);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
