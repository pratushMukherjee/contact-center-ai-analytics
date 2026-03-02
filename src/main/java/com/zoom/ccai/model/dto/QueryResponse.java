package com.zoom.ccai.model.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class QueryResponse {

    private String id;
    private String query;
    private String insight;
    private List<AgentStep> steps;
    private Map<String, Object> metadata;
    private Instant timestamp;

    public QueryResponse() {
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getInsight() { return insight; }
    public void setInsight(String insight) { this.insight = insight; }

    public List<AgentStep> getSteps() { return steps; }
    public void setSteps(List<AgentStep> steps) { this.steps = steps; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
