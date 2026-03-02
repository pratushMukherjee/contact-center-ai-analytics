package com.zoom.ccai.agent.orchestrator;

import com.zoom.ccai.model.dto.AgentStep;
import com.zoom.ccai.model.dto.QueryRequest;
import com.zoom.ccai.model.dto.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Central orchestrator that coordinates the multi-agent ReAct loop.
 * Routes user queries through specialized agents: Planner → Retriever → Analyzer → Summarizer.
 */
@Component
public class AgentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestrator.class);

    private final ReActLoop reactLoop;

    public AgentOrchestrator(ReActLoop reactLoop) {
        this.reactLoop = reactLoop;
    }

    public QueryResponse processQuery(QueryRequest request) {
        String queryId = "q-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Processing query [{}]: {}", queryId, request.getQuery());

        long startTime = System.currentTimeMillis();

        QueryResponse response = reactLoop.execute(
                request.getQuery(),
                request.getConversationId()
        );

        long totalDuration = System.currentTimeMillis() - startTime;

        response.setId(queryId);
        response.setQuery(request.getQuery());

        int totalTokens = response.getSteps().stream()
                .mapToInt(AgentStep::getTokensUsed)
                .sum();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalDurationMs", totalDuration);
        metadata.put("tokensUsed", totalTokens);
        metadata.put("reactIterations", response.getSteps().size());
        metadata.put("model", "gpt-4o-mini");
        response.setMetadata(metadata);

        log.info("Query [{}] completed in {}ms with {} steps",
                queryId, totalDuration, response.getSteps().size());

        return response;
    }
}
