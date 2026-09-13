package io.github.absketches.doodlemini.dto;

import java.util.List;

public record UserAvailabilityResponse(Long userId, String name, String email, List<SlotResponse> slots) {
}
