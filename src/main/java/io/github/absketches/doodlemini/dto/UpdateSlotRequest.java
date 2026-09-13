package io.github.absketches.doodlemini.dto;

import io.github.absketches.doodlemini.entity.SlotStatus;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record UpdateSlotRequest(Instant startTime, Instant endTime, @Positive Long durationMinutes, SlotStatus status) {
}
