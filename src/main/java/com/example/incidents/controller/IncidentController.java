package com.example.incidents.controller;

import com.example.incidents.dto.*;
import com.example.incidents.enums.IncidentStatus;
import com.example.incidents.enums.Severity;
import com.example.incidents.service.IncidentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/incidents")
public class IncidentController {

    private static final Logger log = LoggerFactory.getLogger(IncidentController.class);

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    public ResponseEntity<CreateIncidentApiResponse> createIncident(
            @Valid @RequestBody CreateIncidentRequest request
    ) {
        CreateIncidentResult result = incidentService.createIncident(request);

        boolean created = result.created();

        String message = created
                ? "Incident created successfully"
                : "Incident already exists for externalReferenceId: "
                + result.incident().getExternalReferenceId();

        CreateIncidentApiResponse response =
                new CreateIncidentApiResponse(
                        created,
                        message,
                        result.incident()
                );

        HttpStatus status = created
                ? HttpStatus.CREATED
                : HttpStatus.OK;

        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{id}")
    public IncidentResponse getIncident(@PathVariable UUID id) {
        log.info("event=incident.get.request_received incidentId={}", id);
        return IncidentResponse.from(incidentService.getIncident(id));
    }

    @GetMapping
    public List<IncidentResponse> listIncidents(
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) IncidentStatus status
    ) {
        log.info("event=incident.list.request_received severity={} status={}", severity, status);
        return incidentService.listIncidents(severity, status)
                .stream()
                .map(IncidentResponse::from)
                .toList();
    }

    @PatchMapping("/{id}/status")
    public IncidentResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
        log.info("event=incident.status.update_request incidentId={} newStatus={} updatedBy={}",
                id, request.getStatus(), request.getUpdatedBy());
        return IncidentResponse.from(incidentService.updateStatus(id, request));
    }

    @GetMapping("/{id}/events")
    public List<IncidentEventResponse> getEvents(@PathVariable UUID id) {
        log.info("event=incident.events.request_received incidentId={}", id);
        return incidentService.getIncidentHistory(id)
                .stream()
                .map(IncidentEventResponse::from)
                .toList();
    }
}
