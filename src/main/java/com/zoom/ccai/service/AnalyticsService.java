package com.zoom.ccai.service;

import com.zoom.ccai.repository.CallRecordRepository;
import com.zoom.ccai.repository.AgentPerformanceRepository;
import com.zoom.ccai.repository.CustomerSatisfactionRepository;
import com.zoom.ccai.repository.QueueMetricRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final CallRecordRepository callRecordRepo;
    private final AgentPerformanceRepository agentPerfRepo;
    private final CustomerSatisfactionRepository csatRepo;
    private final QueueMetricRepository queueMetricRepo;

    public AnalyticsService(CallRecordRepository callRecordRepo,
                           AgentPerformanceRepository agentPerfRepo,
                           CustomerSatisfactionRepository csatRepo,
                           QueueMetricRepository queueMetricRepo) {
        this.callRecordRepo = callRecordRepo;
        this.agentPerfRepo = agentPerfRepo;
        this.csatRepo = csatRepo;
        this.queueMetricRepo = queueMetricRepo;
    }

    public Map<String, Object> getHandleTimeByAgent(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = callRecordRepo.findAvgHandleTimeByAgent(start, end);
        Map<String, Object> data = new HashMap<>();
        for (Object[] row : results) {
            data.put((String) row[0], row[1]);
        }
        return data;
    }

    public Map<String, Object> getQueueSummary(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = callRecordRepo.findQueueSummary(start, end);
        Map<String, Object> data = new HashMap<>();
        for (Object[] row : results) {
            Map<String, Object> queueData = new HashMap<>();
            queueData.put("totalCalls", row[1]);
            queueData.put("avgHandleTime", row[2]);
            queueData.put("avgSentiment", row[3]);
            data.put((String) row[0], queueData);
        }
        return data;
    }

    public Map<String, Object> getDispositionBreakdown(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = callRecordRepo.findDispositionBreakdown(start, end);
        Map<String, Object> data = new HashMap<>();
        for (Object[] row : results) {
            data.put((String) row[0], row[1]);
        }
        return data;
    }

    public Map<String, Object> getTopTopics(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = callRecordRepo.findTopTopics(start, end);
        Map<String, Object> data = new HashMap<>();
        for (Object[] row : results) {
            data.put((String) row[0], row[1]);
        }
        return data;
    }

    public List<Object[]> getAgentRankings(LocalDate start, LocalDate end) {
        return agentPerfRepo.findAgentRankings(start, end);
    }

    public Double getAverageCsat(LocalDateTime start, LocalDateTime end) {
        return csatRepo.findAverageCsat(start, end);
    }

    public Map<String, Object> getQueuePerformance(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = queueMetricRepo.findQueuePerformanceSummary(start, end);
        Map<String, Object> data = new HashMap<>();
        for (Object[] row : results) {
            Map<String, Object> queueData = new HashMap<>();
            queueData.put("callsOffered", row[1]);
            queueData.put("callsAnswered", row[2]);
            queueData.put("callsAbandoned", row[3]);
            queueData.put("avgWaitSeconds", row[4]);
            queueData.put("serviceLevelPct", row[5]);
            data.put((String) row[0], queueData);
        }
        return data;
    }
}
