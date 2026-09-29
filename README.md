# Appointment Booking REST API

A Java 21 / Spring Boot REST API for booking appointments. Its core rule: **the system refuses to book two appointments at overlapping times.**

**Live demo:** https://appointment-api-vs1z.onrender.com/swagger-ui.html

> Hosted on a free tier: the first request after a quiet period can take up to a minute while the service wakes up.

## Features

- Create, view, update, and cancel appointments
- Double-booking prevention (overlapping time slots return `409 Conflict`; back-to-back slots are allowed)
- Input validation with clear JSON error responses (`400`, `404`, `409`)
- Filter appointments by status
- Swagger/OpenAPI documentation and a health-check endpoint
- Automated tests and a CI pipeline on every push

## Tech stack

Java 21, Spring Boot, Spring Data JPA, Bean Validation, PostgreSQL (production), H2 (local), JUnit 5, Mockito, MockMvc, Maven, GitHub Actions, Docker, Render

## API endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/appointments` | Create an appointment |
| GET | `/api/appointments` | List appointments (optional `?status=BOOKED` or `CANCELLED`) |
| GET | `/api/appointments/{id}` | Get one appointment |
| PUT | `/api/appointments/{id}` | Update an appointment |
| PATCH | `/api/appointments/{id}/cancel` | Cancel an appointment |

Health check: `/actuator/health`

## Measured results

- **5** REST endpoints
- **24** automated tests (12 service unit tests with Mockito, 11 API integration tests, 1 application-startup test), all passing
- Tests run automatically on every push via GitHub Actions
- **148 ms** average response time for `GET /api/appointments`, measured over 50 sequential requests to the deployed API from a laptop after a warm-up request (fastest 96 ms)

## Run locally

```bash
git clone https://github.com/umamaz/Appointment-API.git
cd Appointment-API
mvn spring-boot:run
```

Then open http://localhost:8080/swagger-ui.html. Locally the app uses an in-memory H2 database.

## Run the tests

```bash
mvn verify
```

## Design notes

- Layered architecture: controller, service, repository, with DTOs separating the API from the database entity
- Overlap rule: two appointments conflict when one starts before the other ends and ends after the other starts
- Production settings come from environment variables (`application-prod.properties`), so no secrets are stored in the code