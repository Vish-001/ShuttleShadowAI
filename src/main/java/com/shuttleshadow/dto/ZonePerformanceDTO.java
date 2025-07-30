package com.shuttleshadow.dto;

import com.shuttleshadow.entities.Zone;

public class ZonePerformanceDTO {
    private Zone zone;
    private double averageReactionTime;
    private int hits;

    public ZonePerformanceDTO() {}

    public ZonePerformanceDTO(Zone zone, double averageReactionTime, int hits) {
        this.zone = zone;
        this.averageReactionTime = averageReactionTime;
        this.hits = hits;
    }

    public ZonePerformanceDTO(String zone, long reactionTime) {
        this.zone = Zone.valueOf(zone);
        this.averageReactionTime = reactionTime;
        this.hits = 1;
    }

    // Getters and setters
    public Zone getZone() { return zone; }
    public void setZone(Zone zone) { this.zone = zone; }

    public double getAverageReactionTime() { return averageReactionTime; }
    public void setAverageReactionTime(double averageReactionTime) { this.averageReactionTime = averageReactionTime; }

    public int getHits() { return hits; }
    public void setHits(int hits) { this.hits = hits; }
}
