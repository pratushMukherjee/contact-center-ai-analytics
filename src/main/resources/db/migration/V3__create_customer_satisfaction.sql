CREATE TABLE customer_satisfaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_id VARCHAR(36) NOT NULL,
    score INT NOT NULL CHECK (score BETWEEN 1 AND 5),
    comment TEXT,
    survey_date TIMESTAMP NOT NULL
);

CREATE INDEX idx_csat_call_id ON customer_satisfaction(call_id);
CREATE INDEX idx_csat_score ON customer_satisfaction(score);
CREATE INDEX idx_csat_date ON customer_satisfaction(survey_date);

CREATE TABLE queue_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    queue_name VARCHAR(50) NOT NULL,
    timestamp_hour TIMESTAMP NOT NULL,
    calls_offered INT NOT NULL,
    calls_answered INT NOT NULL,
    calls_abandoned INT NOT NULL,
    avg_wait_seconds INT NOT NULL,
    service_level_pct DOUBLE
);

CREATE INDEX idx_queue_metrics_name ON queue_metrics(queue_name);
CREATE INDEX idx_queue_metrics_time ON queue_metrics(timestamp_hour);
