package io.github.absketches.doodlemini.service;

import io.github.absketches.doodlemini.entity.Meeting;
import io.github.absketches.doodlemini.entity.SlotStatus;
import io.github.absketches.doodlemini.entity.TimeSlot;
import io.github.absketches.doodlemini.entity.UserAccount;
import io.github.absketches.doodlemini.exception.ConflictException;
import io.github.absketches.doodlemini.exception.InvalidRequestException;
import io.github.absketches.doodlemini.exception.NotFoundException;
import io.github.absketches.doodlemini.repository.MeetingRepository;
import io.github.absketches.doodlemini.repository.TimeSlotRepository;
import io.github.absketches.doodlemini.repository.UserAccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class SlotService {

    private static final Duration DEFAULT_DURATION = Duration.ofMinutes(30);

    private final UserAccountRepository users;
    private final TimeSlotRepository slots;
    private final MeetingRepository meetings;

    public SlotService(UserAccountRepository users, TimeSlotRepository slots, MeetingRepository meetings) {
        this.users = users;
        this.slots = slots;
        this.meetings = meetings;
    }

    @Transactional
    public TimeSlot create(Long userId, Instant startTime, Instant endTime, Long durationMinutes) {
        UserAccount user = users.findWithCalendarById(userId)
                .orElseThrow(() -> new NotFoundException("User was not found."));
        Instant resolvedEnd = resolveEnd(startTime, endTime, durationMinutes, DEFAULT_DURATION);
        validateRange(startTime, resolvedEnd);
        assertNoOverlap(user.getCalendar().getId(), null, startTime, resolvedEnd);
        try {
            return slots.saveAndFlush(new TimeSlot(user.getCalendar(), startTime, resolvedEnd));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Slot overlaps an existing slot for this user.");
        }
    }

    @Transactional(readOnly = true)
    public List<TimeSlot> list(Long userId, Instant from, Instant to, SlotStatus status) {
        validateRange(from, to);
        if (!users.existsById(userId)) {
            throw new NotFoundException("User was not found.");
        }
        if (status == null) {
            return slots.findOverlappingSlotsForUser(
                    userId,
                    from,
                    to);
        }
        return slots.findOverlappingSlotsForUserByStatus(
                userId,
                from,
                to,
                status);
    }

    @Transactional
    public TimeSlot update(Long slotId, Instant startTime, Instant endTime, Long durationMinutes, SlotStatus status) {
        TimeSlot slot = slots.findDetailedById(slotId)
                .orElseThrow(() -> new NotFoundException("Slot was not found."));

        Instant resolvedStart = startTime == null ? slot.getStartTime() : startTime;
        Instant resolvedEnd = resolveUpdatedEnd(slot, resolvedStart, endTime, durationMinutes, startTime != null);
        validateRange(resolvedStart, resolvedEnd);

        if (meetings.existsBySlotId(slotId) && status == SlotStatus.FREE) {
            throw new ConflictException("A slot with a meeting cannot be marked free.");
        }
        assertNoOverlap(slot.getCalendar().getId(), slot.getId(), resolvedStart, resolvedEnd);

        try {
            slot.reschedule(resolvedStart, resolvedEnd);
            if (status != null) {
                slot.mark(status);
            }
            slots.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Slot overlaps an existing slot for this user.");
        }
        return slot;
    }

    @Transactional
    public void delete(Long slotId) {
        TimeSlot slot = slots.findById(slotId)
                .orElseThrow(() -> new NotFoundException("Slot was not found."));
        if (meetings.existsBySlotId(slotId)) {
            throw new ConflictException("A slot with a meeting cannot be deleted.");
        }
        slots.delete(slot);
    }

    @Transactional
    public Meeting book(Long slotId, String title, String description, List<String> participants) {
        try {
            TimeSlot slot = slots.findDetailedById(slotId)
                    .orElseThrow(() -> new NotFoundException("Slot was not found."));
            if (slot.getStatus() != SlotStatus.FREE || meetings.existsBySlotId(slotId)) {
                throw new ConflictException("Only a free, unbooked slot can be converted into a meeting.");
            }
            return meetings.saveAndFlush(new Meeting(slot, title, description, participants.stream().distinct().toList()));
        } catch (OptimisticLockingFailureException ex) {
            throw new ConflictException("Slot was modified concurrently. Retry with the latest slot version.");
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Only a free, unbooked slot can be converted into a meeting.");
        }
    }

    private Instant resolveUpdatedEnd(
            TimeSlot slot,
            Instant resolvedStart,
            Instant endTime,
            Long durationMinutes,
            boolean startChanged) {
        if (endTime != null || durationMinutes != null) {
            return resolveEnd(resolvedStart, endTime, durationMinutes, null);
        }
        if (startChanged) {
            Duration existingDuration = Duration.between(slot.getStartTime(), slot.getEndTime());
            return resolvedStart.plus(existingDuration);
        }
        return slot.getEndTime();
    }

    private Instant resolveEnd(Instant startTime, Instant endTime, Long durationMinutes, Duration defaultDuration) {
        if (endTime != null) {
            return endTime;
        }
        if (durationMinutes != null) {
            return startTime.plus(Duration.ofMinutes(durationMinutes));
        }
        if (defaultDuration != null) {
            return startTime.plus(defaultDuration);
        }
        throw new InvalidRequestException("Provide endTime or durationMinutes.");
    }

    private void validateRange(Instant startTime, Instant endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new InvalidRequestException("endTime must be after startTime.");
        }
    }

    private void assertNoOverlap(Long calendarId, Long excludedSlotId, Instant startTime, Instant endTime) {
        boolean exists = excludedSlotId == null ? slots.existsOverlappingSlot(calendarId, startTime, endTime)
                : slots.existsOverlappingSlotExcluding(
                calendarId,
                excludedSlotId,
                startTime,
                endTime);
        if (exists) {
            throw new ConflictException("Slot overlaps an existing slot for this user.");
        }
    }
}
