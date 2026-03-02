package com.zoom.ccai.observability;

import com.zoom.ccai.model.dto.AgentStep;
import com.zoom.ccai.model.dto.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Evaluation Framework for monitoring AI agent performance in production.
 *
 * Captures and stores evaluation records for every query processed, enabling:
 * - Performance analysis: latency percentiles, token efficiency
 * - Quality tracking: step counts, error rates
 * - Historical review: recent query log with full metadata
 *
 * Records are stored in-memory with a rolling window of the most recent queries.
 * In production, these would be persisted to a time-series database.
 */
@Component
public class EvaluationFramework {

    private static final Logger log = LoggerFactory.getLogger(EvaluationFramework.class);
    private static final int MAX_RECORDS = 100;

    private final Deque<EvaluationRecord> records = new ConcurrentLinkedDeque<>();
    private final AgentMetrics metrics;

    public EvaluationFramework(AgentMetrics metrics) {
        this.metrics = metrics;
    }

    /**
     * Records a completed query for evaluation tracking.
     */
    public void evaluate(QueryResponse response) {
        EvaluationRecord record = new EvaluationRecord();
        record.queryId = response.getId();
        record.query = response.getQuery();
        record.insight = response.getInsight();
        record.timestamp = Instant.now();

        if (response.getSteps() != null) {
            record.stepCount = response.getSteps().size();
            record.totalTokens = response.getSteps().stream()
                    .mapToInt(AgentStep::getTokensUsed).sum();
            record.totalDurationMs = response.getSteps().stream()
                    .mapToLong(AgentStep::getDurationMs).sum();
            record.agentDurations = new LinkedHashMap<>();
            for (AgentStep step : response.getSteps()) {
                record.agentDurations.put(step.getAgent(), step.getDurationMs());
            }
        }

        record.hasError = response.getInsight() != null &&
                response.getInsight().contains("error");

        // Record metrics
        metrics.recordRequest();
        if (record.hasError) metrics.recordError();
        metrics.recordRequestDuration(record.totalDurationMs);
        metrics.recordTokenUsage(record.totalTokens);
        metrics.recordStepCount(record.stepCount);

        for (AgentStep step : response.getSteps()) {
            metrics.recordStepDuration(step.getAgent(), step.getDurationMs());
            if (step.getTokensUsed() > 0) {
                metrics.recordLlmLatency(step.getDurationMs());
            }
        }

        records.addFirst(record);
        while (records.size() > MAX_RECORDS) {
            records.removeLast();
        }

        log.info("Evaluated query [{}]: {}ms, {} steps, {} tokens",
                record.queryId, record.totalDurationMs, record.stepCount, record.totalTokens);
    }

    /**
     * Returns recent evaluation records for the dashboard.
     */
    public List<Map<String, Object>> getRecentEvaluations(int limit) {
        List<Map<String, Object>> results = new ArrayList<>();
        int count = 0;
        for (EvaluationRecord record : records) {
            if (count >= limit) break;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("queryId", record.queryId);
            entry.put("query", record.query);
            entry.put("insightPreview", truncate(record.insight, 150));
            entry.put("stepCount", record.stepCount);
            entry.put("totalDurationMs", record.totalDurationMs);
            entry.put("totalTokens", record.totalTokens);
            entry.put("agentDurations", record.agentDurations);
            entry.put("hasError", record.hasError);
            entry.put("timestamp", record.timestamp.toString());
            results.add(entry);
            count++;
        }
        return results;
    }

    /**
     * Returns aggregate statistics across all evaluations.
     */
    public Map<String, Object> getAggregateStats() {
        if (records.isEmpty()) {
            return Map.of("totalQueries", 0, "message", "No queries processed yet");
        }

        long totalQueries = records.size();
        double avgDuration = records.stream()
                .mapToLong(r -> r.totalDurationMs).average().orElse(0);
        double avgTokens = records.stream()
                .mapToInt(r -> r.totalTokens).average().orElse(0);
        double avgSteps = records.stream()
                .mapToInt(r -> r.stepCount).average().orElse(0);
        long errorCount = records.stream()
                .filter(r -> r.hasError).count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalQueries", totalQueries);
        stats.put("avgDurationMs", Math.round(avgDuration));
        stats.put("avgTokensUsed", Math.round(avgTokens));
        stats.put("avgStepCount", Math.round(avgSteps * 10) / 10.0);
        stats.put("errorRate", Math.round((double) errorCount / totalQueries * 100) / 100.0);
        return stats;
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }

    private static class EvaluationRecord {
        String queryId;
        String query;
        String insight;
        int stepCount;
        int totalTokens;
        long totalDurationMs;
        Map<String, Long> agentDurations;
        boolean hasError;
        Instant timestamp;
    }
}
