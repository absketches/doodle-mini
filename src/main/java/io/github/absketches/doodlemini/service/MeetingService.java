package io.github.absketches.doodlemini.service;

import io.github.absketches.doodlemini.entity.Meeting;
import io.github.absketches.doodlemini.exception.NotFoundException;
import io.github.absketches.doodlemini.repository.MeetingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeetingService {

    private final MeetingRepository meetings;

    public MeetingService(MeetingRepository meetings) {
        this.meetings = meetings;
    }

    @Transactional(readOnly = true)
    public Meeting get(Long meetingId) {
        return meetings.findDetailedById(meetingId)
                .orElseThrow(() -> new NotFoundException("Meeting was not found."));
    }
}
