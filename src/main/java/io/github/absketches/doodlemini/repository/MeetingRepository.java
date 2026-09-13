package io.github.absketches.doodlemini.repository;

import io.github.absketches.doodlemini.entity.Meeting;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    boolean existsBySlotId(Long slotId);

    @EntityGraph(attributePaths = {"participants", "slot", "slot.calendar", "slot.calendar.user"})
    Optional<Meeting> findDetailedById(Long id);
}
