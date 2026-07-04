package com.example.incidents;

import com.example.incidents.repository.IncidentEventRepository;
import com.example.incidents.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IncidentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private IncidentEventRepository incidentEventRepository;

    @BeforeEach
    void cleanDatabase() {
        incidentEventRepository.deleteAll();
        incidentRepository.deleteAll();
    }

    @Test
    void createIncident_shouldReturnCreated_whenValidRequest() throws Exception {
        mockMvc.perform(post("/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Power outage in Building A",
                                  "description": "Main generator failed",
                                  "severity": "HIGH",
                                  "reportedBy": "field-team-1",
                                  "externalReferenceId": "EXT-100"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title").value("Power outage in Building A"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.externalReferenceId").value("EXT-100"));
    }

    @Test
    void createIncident_shouldReturnExistingIncident_whenExternalReferenceIdAlreadyExists() throws Exception {
        String body = """
                {
                  "title": "Weather event",
                  "severity": "MEDIUM",
                  "reportedBy": "field-team-2",
                  "externalReferenceId": "EXT-DUP-1"
                }
                """;

        String firstResponse = mockMvc.perform(post("/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String firstId = extractId(firstResponse);

        mockMvc.perform(post("/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstId))
                .andExpect(jsonPath("$.externalReferenceId").value("EXT-DUP-1"));

        assertThat(incidentRepository.count()).isEqualTo(1);
    }

    @Test
    void createIncident_shouldReturnValidationError_whenTitleMissing() throws Exception {
        mockMvc.perform(post("/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "severity": "HIGH",
                                  "reportedBy": "field-team-1"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[*].field", hasItem("title")));
    }

    @Test
    void createIncident_shouldReturnInvalidRequestValue_whenSeverityIsInvalid() throws Exception {
        mockMvc.perform(post("/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "API failure",
                                  "severity": "URGENT",
                                  "reportedBy": "field-team-1"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST_VALUE"))
                .andExpect(jsonPath("$.message").value("Invalid value for field: severity"))
                .andExpect(jsonPath("$.details[0].field").value("severity"));
    }

    @Test
    void getIncident_shouldReturnNotFound_whenIncidentDoesNotExist() throws Exception {
        UUID missingId = UUID.randomUUID();

        mockMvc.perform(get("/incidents/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("INCIDENT_NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString(missingId.toString())));
    }

    @Test
    void getIncident_shouldReturnInvalidRequestValue_whenIdIsNotUuid() throws Exception {
        mockMvc.perform(get("/incidents/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST_VALUE"))
                .andExpect(jsonPath("$.details[0].field").value("id"));
    }

    @Test
    void updateStatus_shouldCreateIncidentEvent() throws Exception {
        String response = mockMvc.perform(post("/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Facility outage",
                                  "severity": "CRITICAL",
                                  "reportedBy": "ops-user-1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = extractId(response);

        mockMvc.perform(patch("/incidents/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "RESOLVED",
                                  "updatedBy": "ops-user-2"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        mockMvc.perform(get("/incidents/{id}/events", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventType").value("INCIDENT_CREATED"))
                .andExpect(jsonPath("$[1].eventType").value("STATUS_CHANGED"))
                .andExpect(jsonPath("$[1].oldValue").value("OPEN"))
                .andExpect(jsonPath("$[1].newValue").value("RESOLVED"))
                .andExpect(jsonPath("$[1].createdBy").value("ops-user-2"));
    }

    @Test
    void listIncidents_shouldFilterBySeverity() throws Exception {
        mockMvc.perform(post("/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Low issue","severity":"LOW","reportedBy":"team-a"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Critical issue","severity":"CRITICAL","reportedBy":"team-b"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/incidents?severity=CRITICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Critical issue"))
                .andExpect(jsonPath("$[0].severity").value("CRITICAL"));
    }

    @Test
    void listIncidents_shouldFilterBySeverityAndStatus() throws Exception {
        String response = mockMvc.perform(post("/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Critical issue","severity":"CRITICAL","reportedBy":"team-b"}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = extractId(response);

        mockMvc.perform(patch("/incidents/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"IN_PROGRESS","updatedBy":"team-b"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/incidents?severity=CRITICAL&status=IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].severity").value("CRITICAL"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
    }

    private String extractId(String json) {
        Matcher matcher = Pattern.compile("\\\"id\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(json);
        if (!matcher.find()) {
            throw new AssertionError("Could not extract id from response: " + json);
        }
        return matcher.group(1);
    }
}
