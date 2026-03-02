package com.zoom.ccai.controller;

import com.zoom.ccai.agent.orchestrator.AgentOrchestrator;
import com.zoom.ccai.memory.AgentMemoryService;
import com.zoom.ccai.model.dto.AgentStep;
import com.zoom.ccai.model.dto.QueryResponse;
import com.zoom.ccai.observability.EvaluationFramework;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AgentOrchestrator orchestrator;
    @MockBean private AgentMemoryService memoryService;
    @MockBean private EvaluationFramework evaluationFramework;

    @Test
    void submitQuery_shouldReturnInsightWithSteps() throws Exception {
        // Given
        QueryResponse mockResponse = new QueryResponse();
        mockResponse.setId("q-test123");
        mockResponse.setQuery("What is the average handle time?");
        mockResponse.setInsight("Average handle time is 5.2 minutes across all queues.");
        mockResponse.setSteps(List.of(
                new AgentStep("QueryPlanner", "Planning query", "decompose", "Plan", 100, 50),
                new AgentStep("DataRetriever", "Fetching data", "fetch", "{}", 30, 0),
                new AgentStep("Analyzer", "Analyzing data", "analyze", "Analysis", 200, 100),
                new AgentStep("Summarizer", "Generating insight", "summarize", "Summary", 150, 75)
        ));
        mockResponse.setMetadata(Map.of(
                "totalDurationMs", 480L,
                "tokensUsed", 225,
                "reactIterations", 4,
                "model", "gpt-4o-mini"
        ));

        when(orchestrator.processQuery(any())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/analytics/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\": \"What is the average handle time?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("q-test123"))
                .andExpect(jsonPath("$.insight").value("Average handle time is 5.2 minutes across all queues."))
                .andExpect(jsonPath("$.steps").isArray())
                .andExpect(jsonPath("$.steps.length()").value(4))
                .andExpect(jsonPath("$.steps[0].agent").value("QueryPlanner"))
                .andExpect(jsonPath("$.steps[3].agent").value("Summarizer"))
                .andExpect(jsonPath("$.metadata.tokensUsed").value(225));
    }

    @Test
    void submitQuery_shouldRejectBlankQuery() throws Exception {
        mockMvc.perform(post("/api/v1/analytics/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getQueryResult_shouldReturn404ForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/query/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getHistory_shouldReturnEmptyListInitially() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getDashboard_shouldReturnMetrics() throws Exception {
        when(evaluationFramework.getAggregateStats()).thenReturn(
                Map.of("totalQueries", 0, "message", "No queries processed yet"));
        when(evaluationFramework.getRecentEvaluations(5)).thenReturn(List.of());
        when(memoryService.getMemoryStats()).thenReturn(
                Map.of("activeConversations", 0, "totalExchanges", 0, "archivedConversations", 0));

        mockMvc.perform(get("/api/v1/metrics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aggregateStats").exists())
                .andExpect(jsonPath("$.recentQueries").isArray())
                .andExpect(jsonPath("$.memoryStats").exists());
    }
}
