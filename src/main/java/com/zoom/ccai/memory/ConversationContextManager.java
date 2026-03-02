package com.zoom.ccai.memory;

import com.zoom.ccai.model.dto.AgentStep;
import com.zoom.ccai.model.dto.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages conversation context for multi-turn interactions.
 *
 * Maintains a sliding window of conversation history to support follow-up queries
 * like "What about last month?" or "Break that down by queue."
 *
 * Implements token budgeting to prevent context window overflow:
 * - Keeps full detail for the most recent N exchanges
 * - Summarizes older exchanges into compact context
 * - Estimates token count using a 4-chars-per-token heuristic
 */
@Component
public class ConversationContextManager {

    private static final Logger log = LoggerFactory.getLogger(ConversationContextManager.class);

    private static final int MAX_CONTEXT_TOKENS = 4000;
    private static final int MAX_HISTORY_SIZE = 10;
    private static final int CHARS_PER_TOKEN = 4; // Rough estimate

    private final Map<String, ConversationHistory> conversations = new ConcurrentHashMap<>();

    /**
     * Records a completed query exchange in the conversation history.
     */
    public void recordExchange(String conversationId, String query, QueryResponse response) {
        ConversationHistory history = conversations.computeIfAbsent(
                conversationId, k -> new ConversationHistory()
        );

        Exchange exchange = new Exchange();
        exchange.query = query;
        exchange.insight = response.getInsight();
        exchange.stepCount = response.getSteps() != null ? response.getSteps().size() : 0;
        exchange.timestamp = Instant.now();

        history.exchanges.add(exchange);

        // Trim old exchanges beyond max history
        while (history.exchanges.size() > MAX_HISTORY_SIZE) {
            Exchange removed = history.exchanges.remove(0);
            history.summarizedContext = summarizeExchange(history.summarizedContext, removed);
        }

        log.info("Recorded exchange for conversation [{}], total exchanges: {}",
                conversationId, history.exchanges.size());
    }

    /**
     * Builds a context window string for the LLM, respecting token budget.
     * Returns conversation history formatted for inclusion in prompts.
     */
    public String getContextWindow(String conversationId) {
        ConversationHistory history = conversations.get(conversationId);
        if (history == null || history.exchanges.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        int estimatedTokens = 0;

        // Add summarized older context first
        if (history.summarizedContext != null && !history.summarizedContext.isEmpty()) {
            String summary = "Previous conversation summary: " + history.summarizedContext + "\n\n";
            estimatedTokens += summary.length() / CHARS_PER_TOKEN;
            if (estimatedTokens < MAX_CONTEXT_TOKENS) {
                context.append(summary);
            }
        }

        // Add recent exchanges from oldest to newest, respecting token budget
        List<Exchange> recent = history.exchanges;
        for (int i = recent.size() - 1; i >= 0; i--) {
            Exchange ex = recent.get(i);
            String exchangeStr = String.format("User: %s\nInsight: %s\n\n", ex.query, ex.insight);
            int exchangeTokens = exchangeStr.length() / CHARS_PER_TOKEN;

            if (estimatedTokens + exchangeTokens > MAX_CONTEXT_TOKENS) {
                break;
            }
            context.insert(history.summarizedContext != null ? history.summarizedContext.length() + 40 : 0,
                    exchangeStr);
            estimatedTokens += exchangeTokens;
        }

        return context.toString();
    }

    /**
     * Returns the number of exchanges in a conversation.
     */
    public int getExchangeCount(String conversationId) {
        ConversationHistory history = conversations.get(conversationId);
        return history != null ? history.exchanges.size() : 0;
    }

    /**
     * Checks if a conversation exists.
     */
    public boolean hasConversation(String conversationId) {
        return conversations.containsKey(conversationId);
    }

    /**
     * Returns all active conversation IDs.
     */
    public Set<String> getActiveConversationIds() {
        return Collections.unmodifiableSet(conversations.keySet());
    }

    /**
     * Removes a conversation and returns its summary.
     */
    public String archiveConversation(String conversationId) {
        ConversationHistory history = conversations.remove(conversationId);
        if (history == null) return "";

        StringBuilder archive = new StringBuilder();
        if (history.summarizedContext != null) {
            archive.append(history.summarizedContext);
        }
        for (Exchange ex : history.exchanges) {
            archive.append(String.format("Q: %s → A: %s | ",
                    ex.query, truncate(ex.insight, 100)));
        }

        log.info("Archived conversation [{}] with {} exchanges",
                conversationId, history.exchanges.size());

        return archive.toString();
    }

    private String summarizeExchange(String existingSummary, Exchange exchange) {
        String newEntry = String.format("Asked about '%s' (%d steps). ",
                truncate(exchange.query, 50), exchange.stepCount);

        if (existingSummary == null || existingSummary.isEmpty()) {
            return newEntry;
        }
        return existingSummary + newEntry;
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }

    // Inner classes for conversation state
    private static class ConversationHistory {
        List<Exchange> exchanges = new ArrayList<>();
        String summarizedContext;
    }

    private static class Exchange {
        String query;
        String insight;
        int stepCount;
        Instant timestamp;
    }
}
