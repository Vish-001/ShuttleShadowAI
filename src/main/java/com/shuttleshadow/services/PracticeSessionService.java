package com.shuttleshadow.services;

import com.shuttleshadow.dto.ZonePerformanceDTO;
import com.shuttleshadow.entities.PracticeSession;
import com.shuttleshadow.entities.Users;
import com.shuttleshadow.entities.Mode;
import com.shuttleshadow.entities.ZonePerformance;
import java.util.List;

public interface PracticeSessionService {
    PracticeSession startSession(Users user, Mode mode);
    void endSession(Long sessionId);
    List<PracticeSession> getSessionsByUser(Users user);
    PracticeSession getSessionById(Long id);
    void deleteSessionById(Long id);
    void saveSessionResults(Long sessionId, List<ZonePerformanceDTO> results);
    List<ZonePerformance> getWeakZonesForSession(Long sessionId);
    Double getUserAverageReactionTime(Long userId);
}