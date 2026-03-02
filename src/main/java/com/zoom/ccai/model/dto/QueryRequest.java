package com.zoom.ccai.model.dto;

import jakarta.validation.constraints.NotBlank;

public class QueryRequest {

    @NotBlank(message = "Query must not be blank")
    private String query;

    private String conversationId;

    public QueryRequest() {}

    public QueryRequest(String query, String conversationId) {
        this.query = query;
        this.conversationId = conversationId;
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
}
