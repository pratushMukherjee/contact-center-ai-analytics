package com.zoom.ccai.repository;

import com.zoom.ccai.model.entity.AgentPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AgentPerformanceRepository extends JpaRepository<AgentPerformance, Long> {

    List<AgentPerformance> findByAgentName(String agentName);

    List<AgentPerformance> findByDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT ap FROM AgentPerformance ap WHERE ap.agentName = :name AND ap.date BETWEEN :start AND :end")
    List<AgentPerformance> findByAgentAndDateRange(
            @Param("name") String agentName,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT ap.agentName, AVG(ap.avgHandleTime), AVG(ap.avgCsat), AVG(ap.firstCallResolutionRate) " +
           "FROM AgentPerformance ap WHERE ap.date BETWEEN :start AND :end " +
           "GROUP BY ap.agentName ORDER BY AVG(ap.avgCsat) DESC")
    List<Object[]> findAgentRankings(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}
