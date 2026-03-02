CREATE TABLE call_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_id VARCHAR(36) NOT NULL,
    agent_name VARCHAR(100) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    queue_name VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    handle_time_seconds INT NOT NULL,
    hold_time_seconds INT DEFAULT 0,
    wrap_up_seconds INT DEFAULT 0,
    disposition VARCHAR(30) NOT NULL,
    sentiment_score DOUBLE,
    topic VARCHAR(100)
);

CREATE INDEX idx_call_records_agent ON call_records(agent_name);
CREATE INDEX idx_call_records_queue ON call_records(queue_name);
CREATE INDEX idx_call_records_start_time ON call_records(start_time);
CREATE INDEX idx_call_records_disposition ON call_records(disposition);
