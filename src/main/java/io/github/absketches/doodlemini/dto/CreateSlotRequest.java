package io.github.absketches.doodlemini.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record CreateSlotRequest(@NotNull @Future Instant startTime, Instant endTime, @Positive Long durationMinutes) {
}
