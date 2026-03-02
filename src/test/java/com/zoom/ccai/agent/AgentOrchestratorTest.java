package com.zoom.ccai.agent;

import com.zoom.ccai.agent.orchestrator.AgentOrchestrator;
import com.zoom.ccai.agent.orchestrator.ReActLoop;
import com.zoom.ccai.model.dto.AgentStep;
import com.zoom.ccai.model.dto.QueryRequest;
import com.zoom.ccai.model.dto.QueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentOrchestratorTest {

    @Mock private ReActLoop reactLoop;

    private AgentOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new AgentOrchestrator(reactLoop);
    }

    @Test
    void processQuery_shouldReturnResponseWithMetadata() {
        // Given
        QueryRequest request = new QueryRequest("What is the average handle time?", "conv-1");
        QueryResponse mockResponse = new QueryResponse();
        mockResponse.setInsight("Average handle time is 5.2 minutes");
        mockResponse.setSteps(List.of(
                new AgentStep("QueryPlanner", "t", "a", "o", 100, 50),
                new AgentStep("DataRetriever", "t", "a", "o", 30, 0),
                new AgentStep("Analyzer", "t", "a", "o", 200, 100),
                new AgentStep("Summarizer", "t", "a", "o", 150, 75)
        ));
        when(reactLoop.execute(anyString(), anyString())).thenReturn(mockResponse);

        // When
        QueryResponse response = orchestrator.processQuery(request);

        // Then
        assertNotNull(response.getId());
        assertTrue(response.getId().startsWith("q-"));
        assertEquals("What is the average handle time?", response.getQuery());
        assertNotNull(response.getMetadata());
        assertEquals(225, response.getMetadata().get("tokensUsed"));
        assertEquals(4, response.getMetadata().get("reactIterations"));
        assertEquals("gpt-4o-mini", response.getMetadata().get("model"));
    }

    @Test
    void processQuery_shouldGenerateUniqueQueryIds() {
        // Given
        QueryResponse mockResponse = new QueryResponse();
        mockResponse.setSteps(List.of());
        when(reactLoop.execute(anyString(), any())).thenReturn(mockResponse);

        // When
        QueryResponse r1 = orchestrator.processQuery(new QueryRequest("Query 1", null));
        QueryResponse r2 = orchestrator.processQuery(new QueryRequest("Query 2", null));

        // Then
        assertNotEquals(r1.getId(), r2.getId());
    }

    @Test
    void processQuery_shouldSetOriginalQueryInResponse() {
        // Given
        String originalQuery = "Show me CSAT trends for the Support queue";
        QueryResponse mockResponse = new QueryResponse();
        mockResponse.setSteps(List.of());
        when(reactLoop.execute(anyString(), any())).thenReturn(mockResponse);

        // When
        QueryResponse response = orchestrator.processQuery(new QueryRequest(originalQuery, null));

        // Then
        assertEquals(originalQuery, response.getQuery());
    }
}
