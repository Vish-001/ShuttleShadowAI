package com.shuttleshadow.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "zone_performances")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ZonePerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @JsonIgnore
    private PracticeSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Zone zone;

    @Column(name = "average_reaction_time", nullable = false)
    private double averageReactionTime;

    @Column(nullable = false)
    private int hits;

    // Constructors
    public ZonePerformance() {}

    public ZonePerformance(PracticeSession session, Zone zone, double averageReactionTime, int hits) {
        this.session = session;
        this.zone = zone;
        this.averageReactionTime = averageReactionTime;
        this.hits = hits;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PracticeSession getSession() { return session; }
    public void setSession(PracticeSession session) { this.session = session; }
    public Zone getZone() { return zone; }
    public void setZone(Zone zone) { this.zone = zone; }
    public double getAverageReactionTime() { return averageReactionTime; }
    public void setAverageReactionTime(double averageReactionTime) { this.averageReactionTime = averageReactionTime; }
    public int getHits() { return hits; }
    public void setHits(int hits) { this.hits = hits; }
}