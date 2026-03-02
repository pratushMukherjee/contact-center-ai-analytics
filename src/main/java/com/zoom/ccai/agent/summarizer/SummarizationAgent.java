package com.zoom.ccai.agent.summarizer;

import com.zoom.ccai.model.dto.AgentStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Summarization Agent - Final step in the ReAct loop.
 *
 * Takes the analysis findings and generates a clear, actionable natural language
 * response for the user. The summary includes:
 * - Direct answer to the user's question
 * - Key metrics with specific numbers
 * - Actionable recommendations
 * - Context for the findings
 *
 * Uses Chain-of-Thought to ensure the response is coherent and addresses
 * the original question directly.
 */
@Component
public class SummarizationAgent {

    private static final Logger log = LoggerFactory.getLogger(SummarizationAgent.class);

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            You are a Summarization Agent for a Contact Center Analytics system.
            Your job is to take analysis findings and generate a clear, executive-level
            summary that directly answers the user's question.

            Your response should:
            1. Start with a direct answer to the question
            2. Include 2-3 key data points with specific numbers
            3. Provide 1-2 actionable recommendations
            4. Be concise (3-5 sentences maximum)
            5. Use professional but accessible language

            Format: Write a cohesive paragraph, not bullet points.
            Include specific metrics (percentages, averages, counts) to support your points.
            """;

    public SummarizationAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public AgentStep summarize(String analysisResults, String originalQuery, List<AgentStep> previousSteps) {
        long startTime = System.currentTimeMillis();

        String thought = "Generating human-readable insight from analysis results";

        try {
            String stepContext = previousSteps.stream()
                    .map(s -> s.getAgent() + ": " + s.getAction())
                    .collect(Collectors.joining(" → "));

            String summarizePrompt = String.format(
                    "User asked: \"%s\"\n\n" +
                    "Processing pipeline: %s\n\n" +
                    "Analysis findings:\n%s\n\n" +
                    "Generate a concise, actionable summary that directly answers the user's question.",
                    originalQuery, stepContext, analysisResults
            );

            String summary = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(summarizePrompt)
                    .call()
                    .content();

            long duration = System.currentTimeMillis() - startTime;
            int estimatedTokens = (SYSTEM_PROMPT.length() + summarizePrompt.length() + summary.length()) / 4;

            log.info("SummarizationAgent completed in {}ms", duration);

            return new AgentStep(
                    "Summarizer",
                    thought,
                    "generate_insight",
                    summary,
                    duration,
                    estimatedTokens
            );
        } catch (Exception e) {
            log.error("SummarizationAgent failed: {}", e.getMessage());
            long duration = System.currentTimeMillis() - startTime;

            String fallbackSummary = "Based on the analysis of contact center data (Feb 17-28, 2026): " +
                    analysisResults.substring(0, Math.min(analysisResults.length(), 300)) +
                    "\n\n(Note: Full AI-powered summarization requires a valid API key. " +
                    "Set OPENAI_API_KEY environment variable for enhanced insights.)";

            return new AgentStep(
                    "Summarizer",
                    thought,
                    "generate_insight_fallback",
                    fallbackSummary,
                    duration,
                    0
            );
        }
    }
}
