package com.example.incidents.entity;

import com.example.incidents.enums.IncidentStatus;
import com.example.incidents.enums.Severity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "incidents",
        indexes = {
                @Index(name = "idx_incident_severity", columnList = "severity"),
                @Index(name = "idx_incident_status", columnList = "status"),
                @Index(name = "idx_incident_external_ref", columnList = "external_reference_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_incident_external_reference_id", columnNames = "external_reference_id")
        }
)
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentStatus status;

    @Column(nullable = false, length = 120)
    private String reportedBy;

    @Column(name = "external_reference_id", length = 120)
    private String externalReferenceId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = IncidentStatus.OPEN;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void changeStatus(IncidentStatus status) {
        this.status = status;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public String getExternalReferenceId() { return externalReferenceId; }
    public void setExternalReferenceId(String externalReferenceId) { this.externalReferenceId = externalReferenceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

}
