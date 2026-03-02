package com.zoom.ccai.memory;

import com.zoom.ccai.model.dto.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent Memory Service - Manages both short-term and long-term memory for the AI agents.
 *
 * Short-term memory: Active conversation contexts (in-memory ConcurrentHashMap)
 * Long-term memory: Archived conversation summaries for pattern recognition
 *
 * This service coordinates between the ConversationContextManager (per-conversation state)
 * and provides a unified interface for the agent orchestrator.
 */
@Service
public class AgentMemoryService {

    private static final Logger log = LoggerFactory.getLogger(AgentMemoryService.class);

    private final ConversationContextManager contextManager;
    private final Map<String, String> archivedSummaries = new ConcurrentHashMap<>();

    public AgentMemoryService(ConversationContextManager contextManager) {
        this.contextManager = contextManager;
    }

    /**
     * Stores a completed query-response exchange in conversation memory.
     */
    public void remember(String conversationId, String query, QueryResponse response) {
        contextManager.recordExchange(conversationId, query, response);
        log.debug("Stored exchange in conversation [{}]", conversationId);
    }

    /**
     * Retrieves the conversation context window for use in LLM prompts.
     * Respects token budgeting to prevent context overflow.
     */
    public String recall(String conversationId) {
        if (conversationId == null || conversationId.isEmpty()) {
            return "";
        }
        return contextManager.getContextWindow(conversationId);
    }

    /**
     * Archives a conversation to long-term storage and frees short-term memory.
     */
    public void archive(String conversationId) {
        String summary = contextManager.archiveConversation(conversationId);
        if (!summary.isEmpty()) {
            archivedSummaries.put(conversationId, summary);
            log.info("Archived conversation [{}] to long-term memory", conversationId);
        }
    }

    /**
     * Returns memory statistics for observability.
     */
    public Map<String, Object> getMemoryStats() {
        Set<String> activeIds = contextManager.getActiveConversationIds();
        int totalExchanges = activeIds.stream()
                .mapToInt(contextManager::getExchangeCount)
                .sum();

        return Map.of(
                "activeConversations", activeIds.size(),
                "totalExchanges", totalExchanges,
                "archivedConversations", archivedSummaries.size()
        );
    }

    /**
     * Checks if there is prior context for a conversation (for follow-up detection).
     */
    public boolean hasContext(String conversationId) {
        return conversationId != null && contextManager.hasConversation(conversationId);
    }
}
