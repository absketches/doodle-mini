# Doodle Mini

Mini Doodle style meeting scheduling service built with Spring Boot, Java and PostgreSQL.

The service lets users manage available time slots, convert slots into meetings, and query free/busy availability 
over selected time frames. The internal domain contains a user-owned calendar, but the public API is exposed through
users, slots, meetings, and availability.

## Run Locally

```bash
cp .env.example .env
docker compose up --build
```

The API starts on `http://localhost:8080`.

The committed `.env.example` contains local development values only. The real `.env` file is ignored by git, and `application.yml` expects datasource values from the environment.

Useful URLs:

- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`

## Run Tests

```bash
./mvnw test
```
