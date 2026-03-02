package com.zoom.ccai.observability;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Centralized metrics collection for the AI agent system.
 *
 * Registers and manages Micrometer metrics for monitoring agent performance,
 * latency, token usage, and error rates. These metrics are exposed via
 * Prometheus endpoint for production monitoring.
 *
 * Metrics:
 * - ccai.agent.request.count: Total query requests (tagged by status)
 * - ccai.agent.request.duration: End-to-end latency (histogram)
 * - ccai.agent.step.duration: Per-agent-step latency
 * - ccai.agent.step.count: Number of ReAct iterations per query
 * - ccai.agent.tokens.used: Token consumption per request
 * - ccai.agent.llm.latency: Raw LLM API call latency
 * - ccai.agent.error.count: Errors by type
 * - ccai.agent.memory.context.size: Context window utilization
 */
@Component
public class AgentMetrics {

    private final Counter requestCounter;
    private final Counter errorCounter;
    private final Timer requestDuration;
    private final Timer stepDuration;
    private final DistributionSummary tokenUsage;
    private final DistributionSummary stepCount;
    private final Timer llmLatency;
    private final DistributionSummary contextSize;

    public AgentMetrics(MeterRegistry registry) {
        this.requestCounter = Counter.builder("ccai.agent.request.count")
                .description("Total number of analytics queries processed")
                .tag("status", "total")
                .register(registry);

        this.errorCounter = Counter.builder("ccai.agent.error.count")
                .description("Total number of errors during query processing")
                .register(registry);

        this.requestDuration = Timer.builder("ccai.agent.request.duration")
                .description("End-to-end query processing latency")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        this.stepDuration = Timer.builder("ccai.agent.step.duration")
                .description("Per-agent-step processing latency")
                .tag("agent", "all")
                .publishPercentiles(0.5, 0.95)
                .register(registry);

        this.tokenUsage = DistributionSummary.builder("ccai.agent.tokens.used")
                .description("Token consumption per request")
                .publishPercentiles(0.5, 0.95)
                .register(registry);

        this.stepCount = DistributionSummary.builder("ccai.agent.step.count")
                .description("Number of ReAct iterations per query")
                .register(registry);

        this.llmLatency = Timer.builder("ccai.agent.llm.latency")
                .description("Raw LLM API call latency")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        this.contextSize = DistributionSummary.builder("ccai.agent.memory.context.size")
                .description("Context window utilization in estimated tokens")
                .register(registry);
    }

    public void recordRequest() {
        requestCounter.increment();
    }

    public void recordError() {
        errorCounter.increment();
    }

    public void recordRequestDuration(long durationMs) {
        requestDuration.record(Duration.ofMillis(durationMs));
    }

    public void recordStepDuration(String agentName, long durationMs) {
        stepDuration.record(Duration.ofMillis(durationMs));
    }

    public void recordTokenUsage(int tokens) {
        tokenUsage.record(tokens);
    }

    public void recordStepCount(int steps) {
        stepCount.record(steps);
    }

    public void recordLlmLatency(long durationMs) {
        llmLatency.record(Duration.ofMillis(durationMs));
    }

    public void recordContextSize(int estimatedTokens) {
        contextSize.record(estimatedTokens);
    }
}
