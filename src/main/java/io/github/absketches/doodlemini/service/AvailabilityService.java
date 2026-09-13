package io.github.absketches.doodlemini.service;

import io.github.absketches.doodlemini.entity.TimeSlot;
import io.github.absketches.doodlemini.entity.UserAccount;
import io.github.absketches.doodlemini.exception.InvalidRequestException;
import io.github.absketches.doodlemini.exception.NotFoundException;
import io.github.absketches.doodlemini.repository.TimeSlotRepository;
import io.github.absketches.doodlemini.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AvailabilityService {

    private final UserAccountRepository users;
    private final TimeSlotRepository slots;

    public AvailabilityService(UserAccountRepository users, TimeSlotRepository slots) {
        this.users = users;
        this.slots = slots;
    }

    @Transactional(readOnly = true)
    public List<AvailabilityView> getAvailability(List<Long> userIds, Instant from, Instant to) {
        if (userIds == null || userIds.isEmpty()) {
            throw new InvalidRequestException("At least one userId is required.");
        }
        if (!to.isAfter(from)) {
            throw new InvalidRequestException("to must be after from.");
        }

        List<UserAccount> foundUsers = users.findByIdInOrderByIdAsc(userIds);

        Map<Long, AvailabilityView> grouped = new LinkedHashMap<>();
        for (UserAccount user : foundUsers) {
            AvailabilityView view = new AvailabilityView(user.getId(), user.getName(), user.getEmail());
            grouped.put(user.getId(), view);
        }

        long requestedUserCount = userIds.stream().distinct().count();
        if (grouped.size() != requestedUserCount) {
            throw new NotFoundException("One or more users were not found.");
        }

        Collection<Long> foundUserIds = grouped.keySet();
        List<TimeSlot> foundSlots = slots.findOverlappingSlotsForUsers(foundUserIds, from, to);
        foundSlots.forEach(slot -> grouped.get(slot.getCalendar().getUser().getId()).add(slot));
        return List.copyOf(grouped.values());
    }
}
