package io.github.absketches.doodlemini.controller;

import io.github.absketches.doodlemini.dto.AvailabilityResponse;
import io.github.absketches.doodlemini.dto.SlotResponse;
import io.github.absketches.doodlemini.dto.UserAvailabilityResponse;
import io.github.absketches.doodlemini.service.AvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
public class AvailabilityController {

    private final AvailabilityService availability;

    public AvailabilityController(AvailabilityService availability) {
        this.availability = availability;
    }

    @GetMapping("/availability")
    AvailabilityResponse get(
            @RequestParam List<Long> userIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        List<UserAvailabilityResponse> users = availability.getAvailability(userIds, from, to).stream()
                .map(view -> new UserAvailabilityResponse(
                        view.userId(),
                        view.name(),
                        view.email(),
                        view.slots().stream().map(SlotResponse::from).toList()))
                .toList();
        return new AvailabilityResponse(from, to, users);
    }
}
