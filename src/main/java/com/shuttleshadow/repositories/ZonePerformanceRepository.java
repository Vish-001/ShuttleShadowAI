package com.shuttleshadow.repositories;

import com.shuttleshadow.entities.PracticeSession;
import com.shuttleshadow.entities.Zone;
import com.shuttleshadow.entities.ZonePerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ZonePerformanceRepository extends JpaRepository<ZonePerformance, Long> {

    List<ZonePerformance> findBySession(PracticeSession session);

    List<ZonePerformance> findBySessionAndZone(PracticeSession session, Zone zone);

    @Modifying
    @Transactional
    @Query("DELETE FROM ZonePerformance zp WHERE zp.session.id = :sessionId")
    void deleteBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT zp FROM ZonePerformance zp WHERE zp.session = :session AND zp.averageReactionTime > " +
            "(SELECT AVG(zp2.averageReactionTime) FROM ZonePerformance zp2 WHERE zp2.session = :session)")
    List<ZonePerformance> findWeakZonesForSession(@Param("session") PracticeSession session);

    @Query("SELECT AVG(zp.averageReactionTime) FROM ZonePerformance zp WHERE zp.session.user.id = :userId")
    Double findAverageReactionTimeByUser(@Param("userId") Long userId);
}