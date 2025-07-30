package com.shuttleshadow.dto;

import com.shuttleshadow.entities.Zone;
import java.time.LocalDateTime;
import java.util.List;

public class SessionDTO {
    private Long id;
    private String mode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<ZonePerformanceDTO> zonePerformances;
    private Zone weakestZone; // <-- Add this

    // ✅ New constructor with weakestZone
    public SessionDTO(Long id, String mode, LocalDateTime startTime, LocalDateTime endTime,
                      List<ZonePerformanceDTO> zonePerformances, Zone weakestZone) {
        this.id = id;
        this.mode = mode;
        this.startTime = startTime;
        this.endTime = endTime;
        this.zonePerformances = zonePerformances;
        this.weakestZone = weakestZone;
    }

    // ✅ If needed: also keep this constructor without weakestZone
    public SessionDTO(Long id, String mode, LocalDateTime startTime, LocalDateTime endTime,
                      List<ZonePerformanceDTO> zonePerformances) {
        this(id, mode, startTime, endTime, zonePerformances, null);
    }

    // ✅ Getters and setters
    public Long getId() { return id; }
    public String getMode() { return mode; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public List<ZonePerformanceDTO> getZonePerformances() { return zonePerformances; }
    public Zone getWeakestZone() { return weakestZone; }

    public void setId(Long id) { this.id = id; }
    public void setMode(String mode) { this.mode = mode; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public void setZonePerformances(List<ZonePerformanceDTO> zonePerformances) { this.zonePerformances = zonePerformances; }
    public void setWeakestZone(Zone weakestZone) { this.weakestZone = weakestZone; }
}
