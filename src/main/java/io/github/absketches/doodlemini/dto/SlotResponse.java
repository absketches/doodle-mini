package io.github.absketches.doodlemini.dto;

import io.github.absketches.doodlemini.entity.SlotStatus;
import io.github.absketches.doodlemini.entity.TimeSlot;

import java.time.Instant;

public record SlotResponse(Long id, Long userId, Instant startTime, Instant endTime, SlotStatus status, Long version) {

    public static SlotResponse from(TimeSlot slot) {
        return new SlotResponse(
                slot.getId(),
                slot.getCalendar().getUser().getId(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus(),
                slot.getVersion());
    }
}
