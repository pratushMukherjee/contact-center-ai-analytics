package com.zoom.ccai.repository;

import com.zoom.ccai.model.entity.QueueMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QueueMetricRepository extends JpaRepository<QueueMetric, Long> {

    List<QueueMetric> findByQueueName(String queueName);

    @Query("SELECT qm FROM QueueMetric qm WHERE qm.timestampHour BETWEEN :start AND :end ORDER BY qm.timestampHour")
    List<QueueMetric> findByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT qm.queueName, SUM(qm.callsOffered), SUM(qm.callsAnswered), SUM(qm.callsAbandoned), " +
           "AVG(qm.avgWaitSeconds), AVG(qm.serviceLevelPct) " +
           "FROM QueueMetric qm WHERE qm.timestampHour BETWEEN :start AND :end " +
           "GROUP BY qm.queueName")
    List<Object[]> findQueuePerformanceSummary(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
