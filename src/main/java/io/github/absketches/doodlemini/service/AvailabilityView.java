package io.github.absketches.doodlemini.service;

import io.github.absketches.doodlemini.entity.TimeSlot;

import java.util.ArrayList;
import java.util.List;

public final class AvailabilityView {

    private final Long userId;
    private final String name;
    private final String email;
    private final List<TimeSlot> slots = new ArrayList<>();

    AvailabilityView(Long userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public Long userId() {
        return userId;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public List<TimeSlot> slots() {
        return List.copyOf(slots);
    }

    void add(TimeSlot slot) {
        slots.add(slot);
    }
}
