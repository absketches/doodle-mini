package io.github.absketches.doodlemini.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false, unique = true)
    private TimeSlot slot;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingParticipant> participants = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    protected Meeting() {
    }

    public Meeting(TimeSlot slot, String title, String description, List<String> participants) {
        this.slot = slot;
        this.title = title;
        this.description = description;
        participants.forEach(this::addParticipant);
        slot.mark(SlotStatus.BUSY);
    }

    public Long getId() {
        return id;
    }

    public TimeSlot getSlot() {
        return slot;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getParticipants() {
        return participants.stream()
            .map(MeetingParticipant::getParticipant)
            .toList();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private void addParticipant(String participant) {
        participants.add(new MeetingParticipant(this, participant));
    }
}
