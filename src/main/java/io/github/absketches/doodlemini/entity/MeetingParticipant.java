package io.github.absketches.doodlemini.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "meeting_participants",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_meeting_participants_meeting_participant",
        columnNames = {"meeting_id", "participant"}))
public class MeetingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(nullable = false, length = 320)
    private String participant;

    protected MeetingParticipant() {
    }

    MeetingParticipant(Meeting meeting, String participant) {
        this.meeting = meeting;
        this.participant = participant;
    }

    public String getParticipant() {
        return participant;
    }
}

