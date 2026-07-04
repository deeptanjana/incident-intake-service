# Incident Intake Service

A Spring Boot REST service for reporting, tracking, and managing operational incidents such as facility outages, security events, and weather-related disruptions.

## Technology Stack

* Java 17
* Spring Boot 3
* Spring Web
* Spring Data JPA
* H2 Database
* Bean Validation
* Springdoc OpenAPI (Swagger)
* JUnit 5
* Mockito
* MockMvc
* Maven

## Features

* Create new incidents
* Retrieve an incident by its internal UUID
* List incidents with optional filtering by severity and status
* Update incident lifecycle status
* View incident event history
* Idempotent incident creation using `externalReferenceId`
* Consistent machine-readable error responses
* Structured logging for production diagnostics
* Unit and controller tests
* Interactive API documentation with Swagger

---

## Project Structure

```text
src
├── main
│   ├── java
│   │   └── com.example.incident
│   │       ├── api
│   │       ├── config
│   │       ├── domain
│   │       ├── exception
│   │       ├── repository
│   │       └── service
│   └── resources
└── test
```

---

## Running the Application

### Prerequisites

* Java 17
* Maven 3.9+

### Build

```bash
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

---

## Swagger UI

Interactive API documentation is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

---

## REST APIs

### Create Incident

```http
POST /incidents
```

Example request:

```json
{
  "title": "Power outage in Building A",
  "description": "Main generator failure",
  "severity": "HIGH",
  "reportedBy": "field-team-1",
  "externalReferenceId": "EXT-1001"
}
```

Returns:

* **201 Created** – New incident created
* **200 OK** – Existing incident returned when the same `externalReferenceId` is submitted again

---

### Get Incident

```http
GET /incidents/{id}
```

---

### List Incidents

```http
GET /incidents
```

Optional filters:

```http
GET /incidents?severity=HIGH
GET /incidents?status=OPEN
GET /incidents?severity=HIGH&status=OPEN
```

---

### Update Incident Status

```http
PATCH /incidents/{id}/status
```

Example request:

```json
{
  "status": "RESOLVED",
  "updatedBy": "operations-user"
}
```

---

### Get Incident Events

```http
GET /incidents/{id}/events
```

Returns the audit trail associated with an incident.

---

## Design Decisions

### Idempotent Creation

If an `externalReferenceId` is supplied, it is treated as an idempotency key.

Submitting the same reference multiple times returns the existing incident instead of creating a duplicate.

* First request → **201 Created**
* Duplicate request → **200 OK**

---

### Incident Lifecycle

Supported statuses:

* OPEN
* IN_PROGRESS
* RESOLVED
* CLOSED

Each status change creates an audit event.

---

### Incident History

Every significant action generates an event, including:

* INCIDENT_CREATED
* STATUS_CHANGED
* SEVERITY_CHANGED
* INCIDENT_UPDATED

This allows the system to answer:

> "What happened to this incident, and when?"

---

## Error Response Format

All failures return a consistent JSON structure.

Example:

```json
{
  "timestamp": "2026-07-03T18:30:00Z",
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/incidents",
  "details": [
    {
      "field": "title",
      "message": "must not be blank"
    }
  ]
}
```

---

## Structured Logging

Important events include:

```text
incident.create.created
incident.create.idempotent_duplicate_returned
incident.get.found
incident.status.changed
incident.list.executed
api.error.validation_failed
```

Logs include identifiers such as:

* incidentId
* externalReferenceId
* severity
* status
* reportedBy

---

## Running Tests

Execute all tests:

```bash
mvn test
```

The project includes:

* Service unit tests
* Controller tests
* Validation tests
* Idempotency tests
* Exception handling tests

---

## Future Improvements

* Spring Security with JWT/OAuth2
* Flyway/Liquibase migrations
* Pagination and sorting
* Optimistic locking
* Distributed tracing
* Testcontainers
* Metrics and monitoring
* OpenTelemetry integration
