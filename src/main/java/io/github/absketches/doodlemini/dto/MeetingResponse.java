package io.github.absketches.doodlemini.dto;

import io.github.absketches.doodlemini.entity.Meeting;

import java.time.Instant;
import java.util.List;

public record MeetingResponse(Long id, Long slotId, Long userId, Instant startTime, Instant endTime, String title,
                              String description, List<String> participants) {

    public static MeetingResponse from(Meeting meeting) {
        var slot = meeting.getSlot();
        return new MeetingResponse(
                meeting.getId(),
                slot.getId(),
                slot.getCalendar().getUser().getId(),
                slot.getStartTime(),
                slot.getEndTime(),
                meeting.getTitle(),
                meeting.getDescription(),
                meeting.getParticipants());
    }
}
