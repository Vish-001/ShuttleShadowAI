package com.shuttleshadow.repositories;

import com.shuttleshadow.entities.PracticeSession;
import com.shuttleshadow.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PracticeSessionRepository extends JpaRepository<PracticeSession, Long> {

    List<PracticeSession> findByUserOrderByStartTimeDesc(Users user);

    @Modifying
    @Query("DELETE FROM ZonePerformance zp WHERE zp.session.id = :sessionId")
    void deleteZonePerformancesBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT ps FROM PracticeSession ps LEFT JOIN FETCH ps.zonePerformances WHERE ps.user = :user ORDER BY ps.startTime DESC")
    List<PracticeSession> findByUserWithPerformances(@Param("user") Users user);

    // ✅ NEW: Fetch latest session for user that is not ended (endTime is null)
    PracticeSession findTopByUserAndEndTimeIsNullOrderByStartTimeDesc(Users user);
}
