package com.zoom.ccai.controller;

import com.zoom.ccai.agent.orchestrator.AgentOrchestrator;
import com.zoom.ccai.memory.AgentMemoryService;
import com.zoom.ccai.model.dto.QueryRequest;
import com.zoom.ccai.model.dto.QueryResponse;
import com.zoom.ccai.observability.EvaluationFramework;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST API controller for the Contact Center AI Analytics Engine.
 *
 * Endpoints:
 * - POST /api/v1/analytics/query     - Submit a natural language query
 * - GET  /api/v1/analytics/query/{id} - Retrieve a past query result
 * - GET  /api/v1/analytics/history    - List query history for a session
 * - GET  /api/v1/metrics/dashboard    - Agent performance metrics
 */
@RestController
@RequestMapping("/api/v1")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

    private final AgentOrchestrator orchestrator;
    private final AgentMemoryService memoryService;
    private final EvaluationFramework evaluationFramework;

    // In-memory store for query results (production would use a database)
    private final Map<String, QueryResponse> queryStore = new ConcurrentHashMap<>();

    public AnalyticsController(AgentOrchestrator orchestrator,
                               AgentMemoryService memoryService,
                               EvaluationFramework evaluationFramework) {
        this.orchestrator = orchestrator;
        this.memoryService = memoryService;
        this.evaluationFramework = evaluationFramework;
    }

    /**
     * Submit a natural language analytics query.
     *
     * Example:
     *   POST /api/v1/analytics/query
     *   {"query": "Which agents had the highest handle time last week?"}
     */
    @PostMapping("/analytics/query")
    public ResponseEntity<QueryResponse> submitQuery(@Valid @RequestBody QueryRequest request) {
        log.info("Received analytics query: {}", request.getQuery());

        // Generate conversation ID if not provided
        String conversationId = request.getConversationId();
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = "conv-" + UUID.randomUUID().toString().substring(0, 8);
            request.setConversationId(conversationId);
        }

        // Process query through multi-agent orchestrator
        QueryResponse response = orchestrator.processQuery(request);

        // Store result and record in memory + evaluation
        queryStore.put(response.getId(), response);
        memoryService.remember(conversationId, request.getQuery(), response);
        evaluationFramework.evaluate(response);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve a past query result by ID.
     */
    @GetMapping("/analytics/query/{id}")
    public ResponseEntity<QueryResponse> getQueryResult(@PathVariable String id) {
        QueryResponse response = queryStore.get(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * List recent query history.
     */
    @GetMapping("/analytics/history")
    public ResponseEntity<List<Map<String, Object>>> getQueryHistory(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> history = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, QueryResponse> entry : queryStore.entrySet()) {
            if (count >= limit) break;
            QueryResponse qr = entry.getValue();
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("id", qr.getId());
            summary.put("query", qr.getQuery());
            summary.put("insightPreview", truncate(qr.getInsight(), 150));
            summary.put("stepCount", qr.getSteps() != null ? qr.getSteps().size() : 0);
            summary.put("timestamp", qr.getTimestamp().toString());
            history.add(summary);
            count++;
        }
        return ResponseEntity.ok(history);
    }

    /**
     * Agent performance metrics dashboard.
     */
    @GetMapping("/metrics/dashboard")
    public ResponseEntity<Map<String, Object>> getMetricsDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("aggregateStats", evaluationFramework.getAggregateStats());
        dashboard.put("recentQueries", evaluationFramework.getRecentEvaluations(5));
        dashboard.put("memoryStats", memoryService.getMemoryStats());
        return ResponseEntity.ok(dashboard);
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }
}
