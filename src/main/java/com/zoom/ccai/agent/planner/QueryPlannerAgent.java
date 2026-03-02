package com.zoom.ccai.agent.planner;

import com.zoom.ccai.model.dto.AgentStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Query Planner Agent - First step in the ReAct loop.
 *
 * Receives a natural language query and uses Chain-of-Thought prompting
 * to decompose it into a structured execution plan. The plan specifies:
 * - What data needs to be fetched (tables, filters, date ranges)
 * - What analysis operations to perform (aggregation, comparison, trend)
 * - What the final output should contain
 *
 * Uses few-shot examples in the system prompt to ensure consistent JSON output.
 */
@Component
public class QueryPlannerAgent {

    private static final Logger log = LoggerFactory.getLogger(QueryPlannerAgent.class);

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            You are a Query Planner for a Contact Center Analytics system.
            Your job is to decompose natural language questions about contact center data
            into a structured execution plan.

            Available data tables:
            - call_records: Individual call interactions with agent_name, queue_name, handle_time_seconds,
              hold_time_seconds, wrap_up_seconds, disposition (RESOLVED/TRANSFERRED/ESCALATED/ABANDONED),
              sentiment_score (-1 to 1), topic, start_time, end_time
            - agent_performance: Daily agent metrics with calls_handled, avg_handle_time, avg_csat (1-5),
              first_call_resolution_rate (0-1), adherence_rate (0-1)
            - customer_satisfaction: CSAT surveys with score (1-5), comment, call_id
            - queue_metrics: Hourly queue stats with calls_offered, calls_answered, calls_abandoned,
              avg_wait_seconds, service_level_pct

            Available queues: Support, Sales, Billing
            Available agents: Sarah Chen, James Wilson, Maria Garcia, Alex Rivera, Tom Brown,
                            David Kim, Lisa Park, Robert Taylor, Emily Zhang
            Data range: Feb 17 - Feb 28, 2026

            Think step by step about what data is needed and what analysis should be performed.
            Output a clear plan describing:
            1. What data to fetch and any filters
            2. What analysis to perform
            3. What metrics to highlight in the response

            Be specific about date ranges, table names, and column names.
            """;

    public QueryPlannerAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public AgentStep plan(String userQuery, String conversationId) {
        long startTime = System.currentTimeMillis();

        String thought = "Decomposing user query into structured execution plan: " + userQuery;

        try {
            String planResult = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user("Create an execution plan for this query: " + userQuery)
                    .call()
                    .content();

            long duration = System.currentTimeMillis() - startTime;
            int estimatedTokens = (SYSTEM_PROMPT.length() + userQuery.length() + planResult.length()) / 4;

            log.info("QueryPlanner completed in {}ms", duration);

            return new AgentStep(
                    "QueryPlanner",
                    thought,
                    "decompose_query",
                    planResult,
                    duration,
                    estimatedTokens
            );
        } catch (Exception e) {
            log.error("QueryPlanner failed: {}", e.getMessage());
            long duration = System.currentTimeMillis() - startTime;

            // Fallback: generate a basic plan without LLM
            String fallbackPlan = generateFallbackPlan(userQuery);
            return new AgentStep(
                    "QueryPlanner",
                    thought,
                    "decompose_query_fallback",
                    fallbackPlan,
                    duration,
                    0
            );
        }
    }

    private String generateFallbackPlan(String query) {
        String lowerQuery = query.toLowerCase();
        StringBuilder plan = new StringBuilder("Execution Plan (fallback mode):\n");

        if (lowerQuery.contains("handle time") || lowerQuery.contains("aht")) {
            plan.append("1. Fetch: agent_performance or call_records, group by agent_name\n");
            plan.append("2. Analyze: Calculate average handle time, compare to team average\n");
            plan.append("3. Highlight: Top/bottom agents by AHT, trends over time\n");
        } else if (lowerQuery.contains("csat") || lowerQuery.contains("satisfaction")) {
            plan.append("1. Fetch: customer_satisfaction joined with call_records\n");
            plan.append("2. Analyze: Score distribution, average by agent/queue\n");
            plan.append("3. Highlight: CSAT trends, low-scoring areas\n");
        } else if (lowerQuery.contains("queue") || lowerQuery.contains("wait")) {
            plan.append("1. Fetch: queue_metrics for all queues\n");
            plan.append("2. Analyze: Service levels, abandon rates, wait times\n");
            plan.append("3. Highlight: Queue performance comparison\n");
        } else {
            plan.append("1. Fetch: call_records with all available metrics\n");
            plan.append("2. Analyze: General overview of contact center performance\n");
            plan.append("3. Highlight: Key metrics across all dimensions\n");
        }

        return plan.toString();
    }
}
