package io.github.absketches.doodlemini.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.absketches.doodlemini.dto.AvailabilityResponse;
import io.github.absketches.doodlemini.dto.CreateMeetingRequest;
import io.github.absketches.doodlemini.dto.CreateSlotRequest;
import io.github.absketches.doodlemini.dto.CreateUserRequest;
import io.github.absketches.doodlemini.dto.MeetingResponse;
import io.github.absketches.doodlemini.dto.SlotResponse;
import io.github.absketches.doodlemini.dto.UpdateSlotRequest;
import io.github.absketches.doodlemini.dto.UserResponse;
import io.github.absketches.doodlemini.entity.SlotStatus;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DoodleMiniIntegrationTest {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    TestRestTemplate rest;

    @Test
    void createsSlotAndConvertItIntoMeeting() {
        long userId = createUser("Ada Lovelace", "ada@example.test");
        Instant slotStart = futureInstant(1, 9, 0);
        Instant queryFrom = futureInstant(1, 0, 0);
        Instant queryTo = futureInstant(2, 0, 0);
        long slotId = createSlot(userId, slotStart, 45);

        ResponseEntity<MeetingResponse> meeting = rest.postForEntity(
                "/slots/{slotId}/meeting",
                new CreateMeetingRequest(
                        "Planning",
                        "Quarterly planning session",
                        List.of("grace@example.test", "alan@example.test")),
                MeetingResponse.class,
                slotId);

        assertThat(meeting.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        MeetingResponse meetingBody = meeting.getBody();
        assertThat(meetingBody).isNotNull();
        assertThat(meeting.getHeaders().getLocation()).hasPath("/meetings/" + meetingBody.id());
        assertThat(meetingBody)
                .extracting(MeetingResponse::slotId, MeetingResponse::userId, MeetingResponse::title)
                .containsExactly(slotId, userId, "Planning");
        assertThat(meetingBody.participants()).containsExactly("grace@example.test", "alan@example.test");

        ResponseEntity<SlotResponse[]> slots = rest.getForEntity(
                "/users/{userId}/slots?from={from}&to={to}",
                SlotResponse[].class,
                userId,
                queryFrom,
                queryTo);

        assertThat(slots.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(slots.getBody()).singleElement().extracting(SlotResponse::status).isEqualTo(SlotStatus.BUSY);
    }

    @Test
    void rejectsOverlappingSlotsForSameUser() {
        long userId = createUser("Grace Hopper", "grace@example.test");
        createSlot(userId, futureInstant(3, 9, 0), 60);

        ResponseEntity<String> response = rest.postForEntity(
                "/users/{userId}/slots",
                new CreateSlotRequest(futureInstant(3, 9, 30), null, 30L),
                String.class,
                userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Slot overlaps an existing slot for this user.");
    }

    @Test
    void returnsAggregatedAvailability() {
        long firstUserId = createUser("Katherine Johnson", "katherine@example.test");
        long secondUserId = createUser("Mary Jackson", "mary@example.test");

        createSlot(firstUserId, futureInstant(5, 9, 0), 30);
        long busySlotId = createSlot(secondUserId, futureInstant(5, 10, 0), 30);

        ResponseEntity<SlotResponse> updated = rest.exchange(
                "/slots/{slotId}",
                HttpMethod.PATCH,
                new HttpEntity<>(new UpdateSlotRequest(null, null, null, SlotStatus.BUSY)),
                SlotResponse.class,
                busySlotId);

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().status()).isEqualTo(SlotStatus.BUSY);

        ResponseEntity<AvailabilityResponse> availability = rest.getForEntity(
                "/availability?userIds={firstUserId},{secondUserId}&from={from}&to={to}",
                AvailabilityResponse.class,
                firstUserId,
                secondUserId,
                futureInstant(5, 0, 0),
                futureInstant(6, 0, 0));

        assertThat(availability.getStatusCode()).isEqualTo(HttpStatus.OK);
        AvailabilityResponse availabilityBody = availability.getBody();
        assertThat(availabilityBody).isNotNull();
        assertThat(availabilityBody.users()).hasSize(2);
        assertThat(availabilityBody.users())
                .filteredOn(user -> user.userId().equals(firstUserId))
                .singleElement()
                .satisfies(user -> assertThat(user.slots())
                        .singleElement()
                        .extracting(SlotResponse::status)
                        .isEqualTo(SlotStatus.FREE));
        assertThat(availabilityBody.users())
                .filteredOn(user -> user.userId().equals(secondUserId))
                .singleElement()
                .satisfies(user -> assertThat(user.slots())
                        .singleElement()
                        .extracting(SlotResponse::status)
                        .isEqualTo(SlotStatus.BUSY));
    }

    private long createUser(String name, String email) {
        ResponseEntity<UserResponse> response = rest.postForEntity(
                "/users",
                new CreateUserRequest(name, email),
                UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return idFromLocation(response.getHeaders().getLocation());
    }

    private long createSlot(long userId, Instant startTime, long durationMinutes) {
        ResponseEntity<SlotResponse> response = rest.postForEntity(
                "/users/{userId}/slots",
                new CreateSlotRequest(startTime, null, durationMinutes),
                SlotResponse.class,
                userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return idFromLocation(response.getHeaders().getLocation());
    }

    private long idFromLocation(URI location) {
        assertThat(location).isNotNull();
        String path = location.getPath();
        return Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
    }

    private Instant futureInstant(long daysFromNow, int hour, int minute) {
        return LocalDate.now(ZoneOffset.UTC)
                .plusDays(daysFromNow)
                .atTime(LocalTime.of(hour, minute))
                .toInstant(ZoneOffset.UTC);
    }
}
