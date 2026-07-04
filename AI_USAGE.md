# AI_USAGE.md

## AI Assistance

This project was developed with the assistance of ChatGPT as a design and implementation companion. AI was used to accelerate development, evaluate implementation approaches, generate initial code scaffolding, and review the solution. All generated output was reviewed and refined before being incorporated into the final implementation.

## Tools Used

* ChatGPT (OpenAI)

No other AI coding assistants or code generation tools were used during this exercise.

---

## How AI Was Used

AI primarily assisted with:

* Understanding the assignment requirements.
* Designing the domain model and REST API.
* Discussing idempotent handling of `externalReferenceId`.
* Generating initial implementations for entities, DTOs, repositories, services, controllers, exception handling, and tests.
* Suggesting structured logging and consistent error response formats.
* Reviewing the implementation and identifying improvements before completion.
* Drafting project documentation.

---

## Design Decisions Made During Development

Several AI-generated suggestions were reviewed and refined before implementation. Notable decisions include:

* Using **UUIDs** as internal identifiers for incidents.

* Treating `externalReferenceId` as an idempotency key to prevent duplicate incident creation.

* Adding an optional `description` field to provide more detailed incident information.

* Recording `createdBy` within incident history to improve auditability.

* Keeping the incident lifecycle intentionally simple:

    * OPEN
    * IN_PROGRESS
    * RESOLVED
    * CLOSED

* Allowing `GET /incidents` to return all incidents when no filters are supplied while supporting optional filtering.

* Introducing a dedicated `IncidentNotFoundException` for clearer domain-specific error handling.

* Returning `INVALID_REQUEST_VALUE` for invalid enum values instead of treating them as malformed JSON.

* Returning:

    * **201 Created** for new incidents.
    * **200 OK** when an existing incident is returned because of a duplicate `externalReferenceId`.

---

## AI Workflow

Development followed an iterative workflow rather than generating the complete project in a single prompt.

1. Understand the requirements.
2. Design the domain model.
3. Define the REST API.
4. Generate one application layer at a time.
5. Review and refine generated code.
6. Improve exception handling and validation.
7. Add structured logging.
8. Develop focused unit and controller tests.
9. Perform a final design review and document production improvements.

AI served as a development assistant throughout the implementation rather than producing code without review.

---

## Verification

The implementation was verified by:

* Building the project successfully.
* Running the unit and controller tests.
* Exercising the REST APIs through Swagger UI.
* Verifying idempotent behavior for duplicate `externalReferenceId`.
* Confirming consistent validation and exception responses.
* Validating incident history creation during incident creation and lifecycle updates.
* Reviewing structured log output.

---

## Production Improvements

If this service were expanded into a production application, the following enhancements would be considered:

* Flyway or Liquibase database migrations.
* Spring Security with JWT/OAuth2 authentication and authorization.
* Pagination, sorting, and advanced filtering.
* Optimistic locking for concurrent updates.
* Validation of allowed lifecycle transitions.
* Correlation IDs and distributed tracing.
* Testcontainers-based integration testing.
* OpenAPI customization and API versioning.
* Metrics and monitoring with Micrometer and Prometheus.
* OpenTelemetry integration.
* More comprehensive audit metadata, including authenticated user identity and request context.
