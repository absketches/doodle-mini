package io.github.absketches.doodlemini.controller;

import io.github.absketches.doodlemini.dto.CreateMeetingRequest;
import io.github.absketches.doodlemini.dto.CreateSlotRequest;
import io.github.absketches.doodlemini.dto.MeetingResponse;
import io.github.absketches.doodlemini.dto.SlotResponse;
import io.github.absketches.doodlemini.dto.UpdateSlotRequest;
import io.github.absketches.doodlemini.entity.SlotStatus;
import io.github.absketches.doodlemini.exception.InvalidRequestException;
import io.github.absketches.doodlemini.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
public class SlotController {

    private final SlotService slots;

    public SlotController(SlotService slots) {
        this.slots = slots;
    }

    @PostMapping("/users/{userId}/slots")
    ResponseEntity<SlotResponse> create(@PathVariable Long userId, @Valid @RequestBody CreateSlotRequest request) {
        validateCreateRequest(request);
        SlotResponse response = SlotResponse.from(
                slots.create(userId, request.startTime(), request.endTime(), request.durationMinutes()));
        return ResponseEntity.created(URI.create("/slots/" + response.id())).body(response);
    }

    @GetMapping("/users/{userId}/slots")
    List<SlotResponse> list(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) SlotStatus status) {
        return slots.list(userId, from, to, status).stream().map(SlotResponse::from).toList();
    }

    @PatchMapping("/slots/{slotId}")
    SlotResponse update(@PathVariable Long slotId, @Valid @RequestBody UpdateSlotRequest request) {
        validateUpdateRequest(request);
        return SlotResponse.from(slots.update(
                slotId,
                request.startTime(),
                request.endTime(),
                request.durationMinutes(),
                request.status()));
    }

    @DeleteMapping("/slots/{slotId}")
    ResponseEntity<Void> delete(@PathVariable Long slotId) {
        slots.delete(slotId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/slots/{slotId}/meeting")
    ResponseEntity<MeetingResponse> book(
            @PathVariable Long slotId,
            @Valid @RequestBody CreateMeetingRequest request) {
        MeetingResponse response = MeetingResponse.from(
                slots.book(slotId, request.title(), request.description(), request.participants()));
        return ResponseEntity.created(URI.create("/meetings/" + response.id())).body(response);
    }

    private void validateCreateRequest(CreateSlotRequest request) {
        if (request == null) {
            throw new InvalidRequestException("Request body is required.");
        }
        if (request.endTime() != null && request.durationMinutes() != null) {
            throw new InvalidRequestException("Provide either endTime or durationMinutes, not both.");
        }
    }

    private void validateUpdateRequest(UpdateSlotRequest request) {
        if (request == null) {
            throw new InvalidRequestException("Request body is required.");
        }
        boolean hasAnyUpdate = request.startTime() != null
                || request.endTime() != null
                || request.durationMinutes() != null
                || request.status() != null;
        if (!hasAnyUpdate) {
            throw new InvalidRequestException("Provide at least one slot field to update.");
        }
        if (request.endTime() != null && request.durationMinutes() != null) {
            throw new InvalidRequestException("Provide either endTime or durationMinutes, not both.");
        }
    }
}
