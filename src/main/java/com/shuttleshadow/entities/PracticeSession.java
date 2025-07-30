package com.shuttleshadow.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "practice_sessions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PracticeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private Users user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mode mode;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ZonePerformance> zonePerformances = new ArrayList<>();

    public PracticeSession() {}

    public PracticeSession(Users user, Mode mode, LocalDateTime startTime) {
        this.user = user;
        this.mode = mode;
        this.startTime = startTime;
    }

    @Transient
    private String weakestZone;

    public String getWeakestZone() {
        return weakestZone;
    }

    public void setWeakestZone(String weakestZone) {
        this.weakestZone = weakestZone;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }
    public Mode getMode() { return mode; }
    public void setMode(Mode mode) { this.mode = mode; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public List<ZonePerformance> getZonePerformances() { return zonePerformances; }
    public void setZonePerformances(List<ZonePerformance> zonePerformances) {
        this.zonePerformances = zonePerformances;
    }
}