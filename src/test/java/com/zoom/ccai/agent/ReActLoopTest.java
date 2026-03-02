package com.zoom.ccai.agent;

import com.zoom.ccai.agent.analyzer.AnalysisAgent;
import com.zoom.ccai.agent.orchestrator.ReActLoop;
import com.zoom.ccai.agent.planner.QueryPlannerAgent;
import com.zoom.ccai.agent.retriever.DataRetrieverAgent;
import com.zoom.ccai.agent.summarizer.SummarizationAgent;
import com.zoom.ccai.model.dto.AgentStep;
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
class ReActLoopTest {

    @Mock private QueryPlannerAgent plannerAgent;
    @Mock private DataRetrieverAgent retrieverAgent;
    @Mock private AnalysisAgent analysisAgent;
    @Mock private SummarizationAgent summarizationAgent;

    private ReActLoop reactLoop;

    @BeforeEach
    void setUp() {
        reactLoop = new ReActLoop(plannerAgent, retrieverAgent, analysisAgent, summarizationAgent);
    }

    @Test
    void execute_shouldRunAllFourAgentSteps() {
        // Given
        AgentStep planStep = new AgentStep("QueryPlanner", "Planning", "decompose", "Plan output", 100, 50);
        AgentStep retrieveStep = new AgentStep("DataRetriever", "Fetching", "fetch", "{\"data\":[]}", 50, 0);
        AgentStep analyzeStep = new AgentStep("Analyzer", "Analyzing", "analyze", "Analysis result", 200, 100);
        AgentStep summarizeStep = new AgentStep("Summarizer", "Summarizing", "summarize", "Final insight", 150, 75);

        when(plannerAgent.plan(anyString(), any())).thenReturn(planStep);
        when(retrieverAgent.retrieve(anyString(), anyString())).thenReturn(retrieveStep);
        when(analysisAgent.analyze(anyString(), anyString())).thenReturn(analyzeStep);
        when(summarizationAgent.summarize(anyString(), anyString(), anyList())).thenReturn(summarizeStep);

        // When
        QueryResponse response = reactLoop.execute("What is the average handle time?", "conv-123");

        // Then
        assertNotNull(response);
        assertEquals(4, response.getSteps().size());
        assertEquals("Final insight", response.getInsight());
        assertEquals("QueryPlanner", response.getSteps().get(0).getAgent());
        assertEquals("DataRetriever", response.getSteps().get(1).getAgent());
        assertEquals("Analyzer", response.getSteps().get(2).getAgent());
        assertEquals("Summarizer", response.getSteps().get(3).getAgent());
    }

    @Test
    void execute_shouldHandleErrorGracefully() {
        // Given
        when(plannerAgent.plan(anyString(), any())).thenThrow(new RuntimeException("LLM connection failed"));

        // When
        QueryResponse response = reactLoop.execute("Test query", null);

        // Then
        assertNotNull(response);
        assertTrue(response.getInsight().contains("error"));
    }

    @Test
    void execute_shouldPassPlanOutputToRetriever() {
        // Given
        String planOutput = "Fetch call_records grouped by agent_name";
        AgentStep planStep = new AgentStep("QueryPlanner", "Planning", "decompose", planOutput, 100, 50);
        AgentStep retrieveStep = new AgentStep("DataRetriever", "Fetching", "fetch", "{}", 50, 0);
        AgentStep analyzeStep = new AgentStep("Analyzer", "Analyzing", "analyze", "Result", 200, 100);
        AgentStep summarizeStep = new AgentStep("Summarizer", "Summarizing", "summarize", "Done", 150, 75);

        when(plannerAgent.plan(anyString(), any())).thenReturn(planStep);
        when(retrieverAgent.retrieve(eq(planOutput), anyString())).thenReturn(retrieveStep);
        when(analysisAgent.analyze(anyString(), anyString())).thenReturn(analyzeStep);
        when(summarizationAgent.summarize(anyString(), anyString(), anyList())).thenReturn(summarizeStep);

        // When
        reactLoop.execute("Test query", null);

        // Then - verify the plan output was passed to the retriever
        verify(retrieverAgent).retrieve(eq(planOutput), eq("Test query"));
    }

    @Test
    void execute_shouldTrackStepDurations() {
        // Given
        AgentStep planStep = new AgentStep("QueryPlanner", "t", "a", "o", 150, 50);
        AgentStep retrieveStep = new AgentStep("DataRetriever", "t", "a", "o", 30, 0);
        AgentStep analyzeStep = new AgentStep("Analyzer", "t", "a", "o", 250, 100);
        AgentStep summarizeStep = new AgentStep("Summarizer", "t", "a", "o", 200, 75);

        when(plannerAgent.plan(anyString(), any())).thenReturn(planStep);
        when(retrieverAgent.retrieve(anyString(), anyString())).thenReturn(retrieveStep);
        when(analysisAgent.analyze(anyString(), anyString())).thenReturn(analyzeStep);
        when(summarizationAgent.summarize(anyString(), anyString(), anyList())).thenReturn(summarizeStep);

        // When
        QueryResponse response = reactLoop.execute("Test", null);

        // Then
        List<AgentStep> steps = response.getSteps();
        assertEquals(150, steps.get(0).getDurationMs());
        assertEquals(30, steps.get(1).getDurationMs());
        assertEquals(250, steps.get(2).getDurationMs());
        assertEquals(200, steps.get(3).getDurationMs());
    }
}
