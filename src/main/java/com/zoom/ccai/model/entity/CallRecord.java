package com.zoom.ccai.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_records")
public class CallRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_id", nullable = false, length = 36)
    private String callId;

    @Column(name = "agent_name", nullable = false, length = 100)
    private String agentName;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "queue_name", nullable = false, length = 50)
    private String queueName;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "handle_time_seconds", nullable = false)
    private Integer handleTimeSeconds;

    @Column(name = "hold_time_seconds")
    private Integer holdTimeSeconds;

    @Column(name = "wrap_up_seconds")
    private Integer wrapUpSeconds;

    @Column(name = "disposition", nullable = false, length = 30)
    private String disposition;

    @Column(name = "sentiment_score")
    private Double sentimentScore;

    @Column(name = "topic", length = 100)
    private String topic;

    public CallRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCallId() { return callId; }
    public void setCallId(String callId) { this.callId = callId; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getHandleTimeSeconds() { return handleTimeSeconds; }
    public void setHandleTimeSeconds(Integer handleTimeSeconds) { this.handleTimeSeconds = handleTimeSeconds; }

    public Integer getHoldTimeSeconds() { return holdTimeSeconds; }
    public void setHoldTimeSeconds(Integer holdTimeSeconds) { this.holdTimeSeconds = holdTimeSeconds; }

    public Integer getWrapUpSeconds() { return wrapUpSeconds; }
    public void setWrapUpSeconds(Integer wrapUpSeconds) { this.wrapUpSeconds = wrapUpSeconds; }

    public String getDisposition() { return disposition; }
    public void setDisposition(String disposition) { this.disposition = disposition; }

    public Double getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(Double sentimentScore) { this.sentimentScore = sentimentScore; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
}
