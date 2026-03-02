package com.zoom.ccai.model.dto;

public class AgentStep {

    private String agent;
    private String thought;
    private String action;
    private String observation;
    private long durationMs;
    private int tokensUsed;

    public AgentStep() {}

    public AgentStep(String agent, String thought, String action, String observation, long durationMs, int tokensUsed) {
        this.agent = agent;
        this.thought = thought;
        this.action = action;
        this.observation = observation;
        this.durationMs = durationMs;
        this.tokensUsed = tokensUsed;
    }

    public String getAgent() { return agent; }
    public void setAgent(String agent) { this.agent = agent; }

    public String getThought() { return thought; }
    public void setThought(String thought) { this.thought = thought; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public int getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(int tokensUsed) { this.tokensUsed = tokensUsed; }
}
