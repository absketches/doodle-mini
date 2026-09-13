package io.github.absketches.doodlemini.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateMeetingRequest(@NotBlank @Size(max = 200) String title, @Size(max = 2000) String description, @NotEmpty List<@NotBlank @Size(max = 320) String> participants) {
}
