package com.zoom.ccai.repository;

import com.zoom.ccai.model.entity.CustomerSatisfaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerSatisfactionRepository extends JpaRepository<CustomerSatisfaction, Long> {

    List<CustomerSatisfaction> findByCallId(String callId);

    @Query("SELECT AVG(cs.score) FROM CustomerSatisfaction cs WHERE cs.surveyDate BETWEEN :start AND :end")
    Double findAverageCsat(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT cs.score, COUNT(cs) FROM CustomerSatisfaction cs " +
           "WHERE cs.surveyDate BETWEEN :start AND :end GROUP BY cs.score ORDER BY cs.score")
    List<Object[]> findScoreDistribution(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
