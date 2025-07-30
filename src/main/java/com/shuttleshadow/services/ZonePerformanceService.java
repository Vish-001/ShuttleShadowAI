package com.shuttleshadow.services;

import com.shuttleshadow.entities.PracticeSession;
import com.shuttleshadow.entities.ZonePerformance;
import com.shuttleshadow.entities.Zone;

import java.util.List;

public interface ZonePerformanceService {
    void savePerformance(ZonePerformance performance);
    List<ZonePerformance> getBySession(PracticeSession session);
    void deleteBySessionId(Long sessionId);
    List<ZonePerformance> findBySessionAndZone(PracticeSession session, Zone zone);
}