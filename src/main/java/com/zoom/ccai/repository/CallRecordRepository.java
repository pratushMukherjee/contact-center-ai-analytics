package com.zoom.ccai.repository;

import com.zoom.ccai.model.entity.CallRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CallRecordRepository extends JpaRepository<CallRecord, Long> {

    List<CallRecord> findByAgentName(String agentName);

    List<CallRecord> findByQueueName(String queueName);

    List<CallRecord> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT cr FROM CallRecord cr WHERE cr.queueName = :queue AND cr.startTime BETWEEN :start AND :end")
    List<CallRecord> findByQueueAndDateRange(
            @Param("queue") String queueName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT cr.agentName, AVG(cr.handleTimeSeconds) as avgHandleTime " +
           "FROM CallRecord cr WHERE cr.startTime BETWEEN :start AND :end " +
           "GROUP BY cr.agentName ORDER BY avgHandleTime DESC")
    List<Object[]> findAvgHandleTimeByAgent(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT cr.queueName, COUNT(cr) as totalCalls, AVG(cr.handleTimeSeconds) as avgHT, AVG(cr.sentimentScore) as avgSentiment " +
           "FROM CallRecord cr WHERE cr.startTime BETWEEN :start AND :end " +
           "GROUP BY cr.queueName")
    List<Object[]> findQueueSummary(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT cr.disposition, COUNT(cr) FROM CallRecord cr " +
           "WHERE cr.startTime BETWEEN :start AND :end GROUP BY cr.disposition")
    List<Object[]> findDispositionBreakdown(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT cr.topic, COUNT(cr) as cnt FROM CallRecord cr " +
           "WHERE cr.startTime BETWEEN :start AND :end " +
           "GROUP BY cr.topic ORDER BY cnt DESC")
    List<Object[]> findTopTopics(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
