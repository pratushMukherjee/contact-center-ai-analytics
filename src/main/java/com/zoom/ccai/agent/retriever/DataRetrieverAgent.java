package com.zoom.ccai.agent.retriever;

import com.zoom.ccai.model.dto.AgentStep;
import com.zoom.ccai.model.entity.CallRecord;
import com.zoom.ccai.model.entity.AgentPerformance;
import com.zoom.ccai.repository.CallRecordRepository;
import com.zoom.ccai.repository.AgentPerformanceRepository;
import com.zoom.ccai.repository.CustomerSatisfactionRepository;
import com.zoom.ccai.repository.QueueMetricRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Data Retriever Agent - Fetches data from the database based on the planner's output.
 *
 * Translates the execution plan into Spring Data JPA repository calls.
 * Uses the plan keywords to determine which repositories and queries to invoke.
 * Returns structured JSON data for the Analysis Agent.
 */
@Component
public class DataRetrieverAgent {

    private static final Logger log = LoggerFactory.getLogger(DataRetrieverAgent.class);

    private final CallRecordRepository callRecordRepo;
    private final AgentPerformanceRepository agentPerfRepo;
    private final CustomerSatisfactionRepository csatRepo;
    private final QueueMetricRepository queueMetricRepo;
    private final ObjectMapper objectMapper;

    // Default date range for the seed data
    private static final LocalDateTime DEFAULT_START = LocalDateTime.of(2026, 2, 17, 0, 0);
    private static final LocalDateTime DEFAULT_END = LocalDateTime.of(2026, 2, 28, 23, 59);
    private static final LocalDate DEFAULT_DATE_START = LocalDate.of(2026, 2, 17);
    private static final LocalDate DEFAULT_DATE_END = LocalDate.of(2026, 2, 28);

    public DataRetrieverAgent(CallRecordRepository callRecordRepo,
                              AgentPerformanceRepository agentPerfRepo,
                              CustomerSatisfactionRepository csatRepo,
                              QueueMetricRepository queueMetricRepo,
                              ObjectMapper objectMapper) {
        this.callRecordRepo = callRecordRepo;
        this.agentPerfRepo = agentPerfRepo;
        this.csatRepo = csatRepo;
        this.queueMetricRepo = queueMetricRepo;
        this.objectMapper = objectMapper;
    }

    public AgentStep retrieve(String plan, String originalQuery) {
        long startTime = System.currentTimeMillis();

        String thought = "Executing data retrieval based on plan";
        Map<String, Object> retrievedData = new LinkedHashMap<>();

        try {
            String lowerPlan = plan.toLowerCase();
            String lowerQuery = originalQuery.toLowerCase();

            // Determine which data to fetch based on plan and original query
            if (lowerPlan.contains("handle_time") || lowerPlan.contains("handle time")
                    || lowerQuery.contains("handle time") || lowerQuery.contains("aht")) {
                retrievedData.put("avgHandleTimeByAgent",
                        callRecordRepo.findAvgHandleTimeByAgent(DEFAULT_START, DEFAULT_END));
                retrievedData.put("agentPerformance",
                        formatAgentPerformance(agentPerfRepo.findByDateBetween(DEFAULT_DATE_START, DEFAULT_DATE_END)));
            }

            if (lowerPlan.contains("queue") || lowerQuery.contains("queue")
                    || lowerQuery.contains("wait") || lowerQuery.contains("service level")) {
                retrievedData.put("queueSummary",
                        callRecordRepo.findQueueSummary(DEFAULT_START, DEFAULT_END));
                retrievedData.put("queuePerformance",
                        queueMetricRepo.findQueuePerformanceSummary(DEFAULT_START, DEFAULT_END));
            }

            if (lowerPlan.contains("satisfaction") || lowerPlan.contains("csat")
                    || lowerQuery.contains("csat") || lowerQuery.contains("satisfaction")) {
                retrievedData.put("averageCsat",
                        csatRepo.findAverageCsat(DEFAULT_START, DEFAULT_END));
                retrievedData.put("csatDistribution",
                        csatRepo.findScoreDistribution(DEFAULT_START, DEFAULT_END));
            }

            if (lowerPlan.contains("disposition") || lowerQuery.contains("resolution")
                    || lowerQuery.contains("escalat") || lowerQuery.contains("transfer")) {
                retrievedData.put("dispositionBreakdown",
                        callRecordRepo.findDispositionBreakdown(DEFAULT_START, DEFAULT_END));
            }

            if (lowerPlan.contains("topic") || lowerQuery.contains("topic")
                    || lowerQuery.contains("reason") || lowerQuery.contains("why")) {
                retrievedData.put("topTopics",
                        callRecordRepo.findTopTopics(DEFAULT_START, DEFAULT_END));
            }

            if (lowerPlan.contains("agent_performance") || lowerPlan.contains("ranking")
                    || lowerQuery.contains("best") || lowerQuery.contains("worst")
                    || lowerQuery.contains("ranking") || lowerQuery.contains("top agent")) {
                retrievedData.put("agentRankings",
                        agentPerfRepo.findAgentRankings(DEFAULT_DATE_START, DEFAULT_DATE_END));
            }

            // Always include basic overview if nothing specific was matched
            if (retrievedData.isEmpty()) {
                retrievedData.put("avgHandleTimeByAgent",
                        callRecordRepo.findAvgHandleTimeByAgent(DEFAULT_START, DEFAULT_END));
                retrievedData.put("queueSummary",
                        callRecordRepo.findQueueSummary(DEFAULT_START, DEFAULT_END));
                retrievedData.put("dispositionBreakdown",
                        callRecordRepo.findDispositionBreakdown(DEFAULT_START, DEFAULT_END));
                retrievedData.put("averageCsat",
                        csatRepo.findAverageCsat(DEFAULT_START, DEFAULT_END));
            }

            String dataJson = objectMapper.writeValueAsString(retrievedData);
            long duration = System.currentTimeMillis() - startTime;

            log.info("DataRetriever fetched {} data categories in {}ms",
                    retrievedData.size(), duration);

            return new AgentStep(
                    "DataRetriever",
                    thought,
                    "fetch_data",
                    dataJson,
                    duration,
                    0 // No LLM tokens used in data retrieval
            );

        } catch (Exception e) {
            log.error("DataRetriever failed: {}", e.getMessage());
            long duration = System.currentTimeMillis() - startTime;

            return new AgentStep(
                    "DataRetriever",
                    thought,
                    "fetch_data_error",
                    "Error retrieving data: " + e.getMessage(),
                    duration,
                    0
            );
        }
    }

    private List<Map<String, Object>> formatAgentPerformance(List<AgentPerformance> performances) {
        List<Map<String, Object>> formatted = new ArrayList<>();
        for (AgentPerformance ap : performances) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("agentName", ap.getAgentName());
            entry.put("date", ap.getDate().toString());
            entry.put("callsHandled", ap.getCallsHandled());
            entry.put("avgHandleTime", ap.getAvgHandleTime());
            entry.put("avgCsat", ap.getAvgCsat());
            entry.put("fcrRate", ap.getFirstCallResolutionRate());
            entry.put("adherenceRate", ap.getAdherenceRate());
            formatted.add(entry);
        }
        return formatted;
    }
}
