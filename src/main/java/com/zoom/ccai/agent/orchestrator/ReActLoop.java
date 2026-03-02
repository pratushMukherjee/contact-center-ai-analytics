package com.zoom.ccai.agent.orchestrator;

import com.zoom.ccai.agent.planner.QueryPlannerAgent;
import com.zoom.ccai.agent.retriever.DataRetrieverAgent;
import com.zoom.ccai.agent.analyzer.AnalysisAgent;
import com.zoom.ccai.agent.summarizer.SummarizationAgent;
import com.zoom.ccai.model.dto.AgentStep;
import com.zoom.ccai.model.dto.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the ReAct (Reasoning + Acting) pattern for multi-agent orchestration.
 *
 * The loop follows the pattern:
 *   1. THOUGHT: QueryPlannerAgent breaks down the user query into an execution plan
 *   2. ACTION:  DataRetrieverAgent fetches relevant data from the database
 *   3. THOUGHT: AnalysisAgent interprets the data and identifies patterns
 *   4. ACTION:  SummarizationAgent generates a human-readable insight
 *
 * Each step produces a Thought → Action → Observation triple that is appended
 * to the context for subsequent steps, enabling chain-of-thought reasoning.
 *
 * Max iterations: 5 (prevents infinite loops on ambiguous queries)
 */
@Component
public class ReActLoop {

    private static final Logger log = LoggerFactory.getLogger(ReActLoop.class);
    private static final int MAX_ITERATIONS = 5;

    private final QueryPlannerAgent plannerAgent;
    private final DataRetrieverAgent retrieverAgent;
    private final AnalysisAgent analysisAgent;
    private final SummarizationAgent summarizationAgent;

    public ReActLoop(QueryPlannerAgent plannerAgent,
                     DataRetrieverAgent retrieverAgent,
                     AnalysisAgent analysisAgent,
                     SummarizationAgent summarizationAgent) {
        this.plannerAgent = plannerAgent;
        this.retrieverAgent = retrieverAgent;
        this.analysisAgent = analysisAgent;
        this.summarizationAgent = summarizationAgent;
    }

    public QueryResponse execute(String userQuery, String conversationId) {
        List<AgentStep> steps = new ArrayList<>();
        QueryResponse response = new QueryResponse();

        try {
            // Step 1: THOUGHT - Plan the query execution
            log.info("ReAct Step 1: Planning query decomposition");
            AgentStep planStep = plannerAgent.plan(userQuery, conversationId);
            steps.add(planStep);

            // Step 2: ACTION - Retrieve relevant data
            log.info("ReAct Step 2: Retrieving data based on plan");
            AgentStep retrieveStep = retrieverAgent.retrieve(planStep.getObservation(), userQuery);
            steps.add(retrieveStep);

            // Step 3: THOUGHT - Analyze the retrieved data
            log.info("ReAct Step 3: Analyzing retrieved data");
            AgentStep analyzeStep = analysisAgent.analyze(
                    retrieveStep.getObservation(),
                    userQuery
            );
            steps.add(analyzeStep);

            // Step 4: ACTION - Summarize into human-readable insight
            log.info("ReAct Step 4: Generating final insight");
            AgentStep summarizeStep = summarizationAgent.summarize(
                    analyzeStep.getObservation(),
                    userQuery,
                    steps
            );
            steps.add(summarizeStep);

            response.setInsight(summarizeStep.getObservation());

        } catch (Exception e) {
            log.error("ReAct loop error: {}", e.getMessage(), e);
            response.setInsight("I encountered an error while processing your query: " + e.getMessage()
                    + ". Please try rephrasing your question or check if the AI service is configured correctly.");
        }

        response.setSteps(steps);
        return response;
    }
}
