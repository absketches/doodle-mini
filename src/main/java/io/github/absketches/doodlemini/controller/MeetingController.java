package io.github.absketches.doodlemini.controller;

import io.github.absketches.doodlemini.dto.MeetingResponse;
import io.github.absketches.doodlemini.service.MeetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meetings")
public class MeetingController {

    private final MeetingService meetings;

    public MeetingController(MeetingService meetings) {
        this.meetings = meetings;
    }

    @GetMapping("/{meetingId}")
    MeetingResponse get(@PathVariable Long meetingId) {
        return MeetingResponse.from(meetings.get(meetingId));
    }
}
