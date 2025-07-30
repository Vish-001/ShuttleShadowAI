package com.shuttleshadow.services.impl;

import com.shuttleshadow.dto.ZonePerformanceDTO;
import com.shuttleshadow.entities.*;
import com.shuttleshadow.exceptions.SessionNotFoundException;
import com.shuttleshadow.repositories.PracticeSessionRepository;
import com.shuttleshadow.repositories.ZonePerformanceRepository;
import com.shuttleshadow.services.PracticeSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PracticeSessionServiceImpl implements PracticeSessionService {

    private final PracticeSessionRepository sessionRepository;
    private final ZonePerformanceRepository zonePerformanceRepository;

    @Autowired
    public PracticeSessionServiceImpl(
            PracticeSessionRepository sessionRepository,
            ZonePerformanceRepository zonePerformanceRepository) {
        this.sessionRepository = sessionRepository;
        this.zonePerformanceRepository = zonePerformanceRepository;
    }

    @Override
    @Transactional
    public PracticeSession startSession(Users user, Mode mode) {
        // Option 2: Return existing active session if it exists
        PracticeSession existingActive = sessionRepository
                .findTopByUserAndEndTimeIsNullOrderByStartTimeDesc(user);

        if (existingActive != null) {
            return existingActive;
        }

        // Otherwise create new session
        PracticeSession session = new PracticeSession();
        session.setUser(user);
        session.setMode(mode);
        session.setStartTime(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    @Override
    @Transactional
    public void endSession(Long sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setEndTime(LocalDateTime.now());
            sessionRepository.save(session);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<PracticeSession> getSessionsByUser(Users user) {
        return sessionRepository.findByUserOrderByStartTimeDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PracticeSession getSessionById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteSessionById(Long id) {
        zonePerformanceRepository.deleteBySessionId(id);
        sessionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void saveSessionResults(Long sessionId, List<ZonePerformanceDTO> results) {
        if (sessionId == null || sessionId <= 0) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        PracticeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // Validate input
        results.forEach(dto -> {
            if (dto.getAverageReactionTime() <= 0) {
                throw new IllegalArgumentException("Invalid reaction time for zone " + dto.getZone());
            }
            if (dto.getHits() <= 0) {
                throw new IllegalArgumentException("Invalid hit count for zone " + dto.getZone());
            }
        });

        // End the session
        session.setEndTime(LocalDateTime.now());

        // Clear previous results
        zonePerformanceRepository.deleteBySessionId(sessionId);

        // Save new results
        results.forEach(result -> {
            ZonePerformance zp = new ZonePerformance();
            zp.setSession(session);
            zp.setZone(result.getZone());
            zp.setAverageReactionTime(result.getAverageReactionTime());
            zp.setHits(result.getHits());
            zonePerformanceRepository.save(zp);
        });

        sessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZonePerformance> getWeakZonesForSession(Long sessionId) {
        PracticeSession session = getSessionById(sessionId);
        return zonePerformanceRepository.findWeakZonesForSession(session);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getUserAverageReactionTime(Long userId) {
        return zonePerformanceRepository.findAverageReactionTimeByUser(userId);
    }

}
