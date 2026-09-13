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

Integration tests use PostgreSQL Testcontainers, so Docker must be running.

## API Examples

Create a user:

```bash
curl -i -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Abhi Basu",
    "email": "abhi@example.com"
  }'
```

Create an available slot using a configurable duration:

```bash
curl -i -X POST http://localhost:8080/users/1/slots \
  -H 'Content-Type: application/json' \
  -d '{
    "startTime": "2030-01-10T09:00:00Z",
    "durationMinutes": 45
  }'
```

List a user's slots in a time frame:

```bash
curl 'http://localhost:8080/users/1/slots?from=2030-01-10T00:00:00Z&to=2030-01-11T00:00:00Z'
```

Filter by status:

```bash
curl 'http://localhost:8080/users/1/slots?from=2030-01-10T00:00:00Z&to=2030-01-11T00:00:00Z&status=FREE'
```

Update a slot time or status:

```bash
curl -i -X PATCH http://localhost:8080/slots/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "status": "BUSY"
  }'
```

Delete an unbooked slot:

```bash
curl -i -X DELETE http://localhost:8080/slots/2
```

Convert a free slot into a meeting:

```bash
curl -i -X POST http://localhost:8080/slots/1/meeting \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Planning",
    "description": "Quarterly planning session",
    "participants": [
      "abhi@example.com",
      "basu@example.com"
    ]
  }'
```

Get a meeting:

```bash
curl http://localhost:8080/meetings/1
```

Query aggregated availability:

```bash
curl 'http://localhost:8080/availability?userIds=1,2&from=2030-01-10T00:00:00Z&to=2030-01-11T00:00:00Z'
```

## Important Rules

- Times are accepted and returned as ISO-8601 instants.
- Slots for the same user cannot overlap.
- A slot starts as `FREE`.
- A slot can be marked `FREE` or `BUSY`.
- Only a `FREE` unbooked slot can be converted into a meeting.
- Converting a slot into a meeting marks the slot `BUSY`.
- A booked slot cannot be deleted.
- Availability queries require bounded `from` and `to` parameters.


## Limitations not able to address due to time

- Slot overlap is enforced in the service layer, not as a PostgreSQL exclusion constraint. 
Two concurrent requests could theoretically create overlapping slots unless we implement a stronger constraint or locking strategy.
- Meeting participants are stored as email-like strings and are not linked to user accounts. Booking a meeting marks the owner’s slot busy, but it does not reserve time in participant calendars or validate participant availability.
- Availability returns declared slots within a time frame.
- Meeting lifecycle support is minimal. The service supports converting a slot into a meeting and fetching the meeting, but not updating meeting details, cancelling meetings, etc.
- User management is limited. Users can be created and fetched, but there is no authentication, authorization, profile update, deletion or ownership enforcement.
- Query APIs are bounded by `from` and `to`, but there is no pagination, maximum query window, or maximum number of requested users. For much larger datasets, those limits need to be added.
- The API uses UTC instants only. It does not model user time zones, daylight-saving behavior, local business hours or recurring slots.
- Test coverage focuses on core service behavior and main integration flows. More time would add tests for validation errors, concurrent booking/overlap scenarios and edge cases around large query ranges.
