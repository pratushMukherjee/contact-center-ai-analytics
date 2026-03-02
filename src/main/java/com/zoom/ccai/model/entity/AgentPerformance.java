package com.zoom.ccai.model.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "agent_performance")
public class AgentPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_name", nullable = false, length = 100)
    private String agentName;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "calls_handled", nullable = false)
    private Integer callsHandled;

    @Column(name = "avg_handle_time", nullable = false)
    private Double avgHandleTime;

    @Column(name = "avg_csat")
    private Double avgCsat;

    @Column(name = "first_call_resolution_rate")
    private Double firstCallResolutionRate;

    @Column(name = "adherence_rate")
    private Double adherenceRate;

    public AgentPerformance() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getCallsHandled() { return callsHandled; }
    public void setCallsHandled(Integer callsHandled) { this.callsHandled = callsHandled; }

    public Double getAvgHandleTime() { return avgHandleTime; }
    public void setAvgHandleTime(Double avgHandleTime) { this.avgHandleTime = avgHandleTime; }

    public Double getAvgCsat() { return avgCsat; }
    public void setAvgCsat(Double avgCsat) { this.avgCsat = avgCsat; }

    public Double getFirstCallResolutionRate() { return firstCallResolutionRate; }
    public void setFirstCallResolutionRate(Double firstCallResolutionRate) { this.firstCallResolutionRate = firstCallResolutionRate; }

    public Double getAdherenceRate() { return adherenceRate; }
    public void setAdherenceRate(Double adherenceRate) { this.adherenceRate = adherenceRate; }
}
