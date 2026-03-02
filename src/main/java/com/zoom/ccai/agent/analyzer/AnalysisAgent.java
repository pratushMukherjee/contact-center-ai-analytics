package com.zoom.ccai.agent.analyzer;

import com.zoom.ccai.model.dto.AgentStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Analysis Agent - Interprets raw data and identifies patterns, trends, and anomalies.
 *
 * Receives structured data from the DataRetrieverAgent and uses the LLM
 * to perform statistical analysis:
 * - Identify outliers and anomalies
 * - Compare metrics across agents/queues
 * - Detect trends over time
 * - Correlate metrics (e.g., high handle time → low CSAT)
 *
 * Outputs structured analysis findings for the SummarizationAgent.
 */
@Component
public class AnalysisAgent {

    private static final Logger log = LoggerFactory.getLogger(AnalysisAgent.class);

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            You are a Data Analysis Agent for a Contact Center Analytics system.
            You receive raw data from database queries and must analyze it to find patterns,
            trends, and actionable insights.

            Your analysis should:
            1. Identify key metrics and their values
            2. Compare performance across agents, queues, or time periods
            3. Detect anomalies (values significantly above/below average)
            4. Find correlations between metrics
            5. Provide specific numbers and percentages

            Contact Center KPI context:
            - Average Handle Time (AHT): Lower is generally better. >600s is high for Support.
            - CSAT: Scale 1-5. >4.0 is good, <3.0 needs attention.
            - First Call Resolution (FCR): >0.8 is good, <0.5 is concerning.
            - Service Level: >0.80 (80%) is industry standard target.
            - Sentiment: -1 to 1. Positive >0.3, Negative <-0.3.

            Format your analysis as structured findings with specific data points.
            """;

    public AnalysisAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public AgentStep analyze(String rawData, String originalQuery) {
        long startTime = System.currentTimeMillis();

        String thought = "Analyzing retrieved data to identify patterns and insights";

        try {
            String analysisPrompt = String.format(
                    "Analyze this contact center data to answer: \"%s\"\n\nData:\n%s\n\n" +
                    "Provide specific findings with numbers. Identify the top issues and recommendations.",
                    originalQuery, rawData
            );

            String analysisResult = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(analysisPrompt)
                    .call()
                    .content();

            long duration = System.currentTimeMillis() - startTime;
            int estimatedTokens = (SYSTEM_PROMPT.length() + analysisPrompt.length() + analysisResult.length()) / 4;

            log.info("AnalysisAgent completed in {}ms", duration);

            return new AgentStep(
                    "Analyzer",
                    thought,
                    "analyze_data",
                    analysisResult,
                    duration,
                    estimatedTokens
            );
        } catch (Exception e) {
            log.error("AnalysisAgent failed: {}", e.getMessage());
            long duration = System.currentTimeMillis() - startTime;

            String fallbackAnalysis = generateFallbackAnalysis(rawData);
            return new AgentStep(
                    "Analyzer",
                    thought,
                    "analyze_data_fallback",
                    fallbackAnalysis,
                    duration,
                    0
            );
        }
    }

    private String generateFallbackAnalysis(String rawData) {
        return "Analysis (fallback mode - LLM unavailable):\n" +
               "Raw data has been retrieved successfully. Key observations:\n" +
               "- Data contains metrics across multiple agents and queues\n" +
               "- Time period covers February 17-28, 2026\n" +
               "- Full LLM-powered analysis requires a valid API key\n\n" +
               "Data preview: " + rawData.substring(0, Math.min(rawData.length(), 500));
    }
}
