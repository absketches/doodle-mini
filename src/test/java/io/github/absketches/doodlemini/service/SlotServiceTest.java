package io.github.absketches.doodlemini.service;

import io.github.absketches.doodlemini.entity.Meeting;
import io.github.absketches.doodlemini.entity.SlotStatus;
import io.github.absketches.doodlemini.entity.TimeSlot;
import io.github.absketches.doodlemini.entity.UserAccount;
import io.github.absketches.doodlemini.exception.ConflictException;
import io.github.absketches.doodlemini.repository.MeetingRepository;
import io.github.absketches.doodlemini.repository.TimeSlotRepository;
import io.github.absketches.doodlemini.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SlotServiceTest {

    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final TimeSlotRepository slots = mock(TimeSlotRepository.class);
    private final MeetingRepository meetings = mock(MeetingRepository.class);
    private final SlotService service = new SlotService(users, slots, meetings);

    @Test
    void bookConvertsFreeSlotIntoMeeting() {
        Long slotId = 42L;
        TimeSlot slot = freeSlot();

        when(slots.findDetailedById(slotId)).thenReturn(Optional.of(slot));
        when(meetings.existsBySlotId(slotId)).thenReturn(false);
        when(meetings.saveAndFlush(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Meeting meeting = service.book(
                slotId,
                "Planning",
                "Quarterly planning session",
                List.of("grace@gmail.com", "grace@gmail.com", "alan@gmail.com"));

        assertThat(meeting.getSlot()).isSameAs(slot);
        assertThat(meeting.getTitle()).isEqualTo("Planning");
        assertThat(meeting.getDescription()).isEqualTo("Quarterly planning session");
        assertThat(meeting.getParticipants()).containsExactly("grace@gmail.com", "alan@gmail.com");
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.BUSY);
    }

    @Test
    void bookRejectsBusySlot() {
        Long slotId = 42L;
        TimeSlot slot = freeSlot();
        slot.mark(SlotStatus.BUSY);

        when(slots.findDetailedById(slotId)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> service.book(slotId, "Planning", null, List.of("grace@gmail.com")))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Only a free, unbooked slot can be converted into a meeting.");

        verify(meetings, never()).saveAndFlush(any());
    }

    @Test
    void bookMapsDuplicateMeetingInsertToConflict() {
        Long slotId = 42L;
        TimeSlot slot = freeSlot();

        when(slots.findDetailedById(slotId)).thenReturn(Optional.of(slot));
        when(meetings.existsBySlotId(slotId)).thenReturn(false);
        when(meetings.saveAndFlush(any(Meeting.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.book(slotId, "Planning", null, List.of("grace@gmail.com")))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Only a free, unbooked slot can be converted into a meeting.");
    }

    private TimeSlot freeSlot() {
        UserAccount user = new UserAccount("Dennis Richie", "dRichie@gmail.com");
        return new TimeSlot(
                user.getCalendar(),
                Instant.parse("2030-01-10T09:00:00Z"),
                Instant.parse("2030-01-10T09:30:00Z"));
    }
}
