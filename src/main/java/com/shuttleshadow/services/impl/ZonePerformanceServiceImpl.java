package com.shuttleshadow.services.impl;

import com.shuttleshadow.entities.PracticeSession;
import com.shuttleshadow.entities.Zone;
import com.shuttleshadow.entities.ZonePerformance;
import com.shuttleshadow.repositories.ZonePerformanceRepository;
import com.shuttleshadow.services.ZonePerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ZonePerformanceServiceImpl implements ZonePerformanceService {

    @Autowired
    private ZonePerformanceRepository zonePerformanceRepository;

    @Override
    @Transactional
    public void savePerformance(ZonePerformance performance) {
        zonePerformanceRepository.save(performance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZonePerformance> getBySession(PracticeSession session) {
        return zonePerformanceRepository.findBySession(session);
    }

    @Override
    @Transactional
    public void deleteBySessionId(Long sessionId) {
        zonePerformanceRepository.deleteBySessionId(sessionId);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ZonePerformance> findBySessionAndZone(PracticeSession session, Zone zone) {
        return zonePerformanceRepository.findBySessionAndZone(session, zone);
    }
}