package com.zoom.ccai.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_metrics")
public class QueueMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "queue_name", nullable = false, length = 50)
    private String queueName;

    @Column(name = "timestamp_hour", nullable = false)
    private LocalDateTime timestampHour;

    @Column(name = "calls_offered", nullable = false)
    private Integer callsOffered;

    @Column(name = "calls_answered", nullable = false)
    private Integer callsAnswered;

    @Column(name = "calls_abandoned", nullable = false)
    private Integer callsAbandoned;

    @Column(name = "avg_wait_seconds", nullable = false)
    private Integer avgWaitSeconds;

    @Column(name = "service_level_pct")
    private Double serviceLevelPct;

    public QueueMetric() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }

    public LocalDateTime getTimestampHour() { return timestampHour; }
    public void setTimestampHour(LocalDateTime timestampHour) { this.timestampHour = timestampHour; }

    public Integer getCallsOffered() { return callsOffered; }
    public void setCallsOffered(Integer callsOffered) { this.callsOffered = callsOffered; }

    public Integer getCallsAnswered() { return callsAnswered; }
    public void setCallsAnswered(Integer callsAnswered) { this.callsAnswered = callsAnswered; }

    public Integer getCallsAbandoned() { return callsAbandoned; }
    public void setCallsAbandoned(Integer callsAbandoned) { this.callsAbandoned = callsAbandoned; }

    public Integer getAvgWaitSeconds() { return avgWaitSeconds; }
    public void setAvgWaitSeconds(Integer avgWaitSeconds) { this.avgWaitSeconds = avgWaitSeconds; }

    public Double getServiceLevelPct() { return serviceLevelPct; }
    public void setServiceLevelPct(Double serviceLevelPct) { this.serviceLevelPct = serviceLevelPct; }
}
