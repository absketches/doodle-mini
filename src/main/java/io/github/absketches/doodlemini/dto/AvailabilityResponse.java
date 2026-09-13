package io.github.absketches.doodlemini.dto;

import java.time.Instant;
import java.util.List;

public record AvailabilityResponse(Instant from, Instant to, List<UserAvailabilityResponse> users) {
}
