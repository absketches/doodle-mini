package io.github.absketches.doodlemini.repository;

import io.github.absketches.doodlemini.entity.SlotStatus;
import io.github.absketches.doodlemini.entity.TimeSlot;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    @EntityGraph(attributePaths = {"calendar", "calendar.user"})
    Optional<TimeSlot> findDetailedById(Long id);

    @Query("""
            select case when count(slot) > 0 then true else false end
            from TimeSlot slot
            where slot.calendar.id = :calendarId
            and slot.startTime < :endTime
            and slot.endTime > :startTime
            """)
    boolean existsOverlappingSlot(
            @Param("calendarId") Long calendarId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    @Query("""
            select case when count(slot) > 0 then true else false end
            from TimeSlot slot
            where slot.calendar.id = :calendarId
            and slot.id <> :excludedSlotId
            and slot.startTime < :endTime
            and slot.endTime > :startTime
            """)
    boolean existsOverlappingSlotExcluding(
            @Param("calendarId") Long calendarId,
            @Param("excludedSlotId") Long excludedSlotId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    @EntityGraph(attributePaths = {"calendar", "calendar.user"})
    @Query("""
            select slot
            from TimeSlot slot
            where slot.calendar.user.id = :userId
            and slot.startTime < :to
            and slot.endTime > :from
            order by slot.startTime
            """)
    List<TimeSlot> findOverlappingSlotsForUser(
            @Param("userId") Long userId,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @EntityGraph(attributePaths = {"calendar", "calendar.user"})
    @Query("""
            select slot
            from TimeSlot slot
            where slot.calendar.user.id = :userId
            and slot.startTime < :to
            and slot.endTime > :from
            and slot.status = :status
            order by slot.startTime
            """)
    List<TimeSlot> findOverlappingSlotsForUserByStatus(
            @Param("userId") Long userId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("status") SlotStatus status);

    @EntityGraph(attributePaths = {"calendar", "calendar.user"})
    @Query("""
            select slot
            from TimeSlot slot
            where slot.calendar.user.id in :userIds
            and slot.startTime < :to
            and slot.endTime > :from
            order by slot.calendar.user.id, slot.startTime
            """)
    List<TimeSlot> findOverlappingSlotsForUsers(
            @Param("userIds") Collection<Long> userIds,
            @Param("from") Instant from,
            @Param("to") Instant to);
}
