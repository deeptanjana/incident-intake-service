package com.example.incidents.repository;

import com.example.incidents.entity.IncidentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IncidentEventRepository extends JpaRepository<IncidentEvent, UUID> {
    List<IncidentEvent> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
