CREATE TABLE agent_performance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agent_name VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    calls_handled INT NOT NULL,
    avg_handle_time DOUBLE NOT NULL,
    avg_csat DOUBLE,
    first_call_resolution_rate DOUBLE,
    adherence_rate DOUBLE
);

CREATE INDEX idx_agent_perf_name ON agent_performance(agent_name);
CREATE INDEX idx_agent_perf_date ON agent_performance(date);
