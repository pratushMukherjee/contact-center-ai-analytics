# Contact Center AI Analytics Engine

A multi-agent AI analytics engine for contact center data, built with **Java 17**, **Spring Boot 3.4**, and **Spring AI**. Uses **ReAct (Reasoning + Acting)** patterns to autonomously plan and execute complex analytical workflows.

> Built as a portfolio project demonstrating AI-native architectures for contact center analytics — the same domain as [Zoom Contact Center's AI-powered analytics team](https://zoom.us).

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    REST API Layer                             │
│  POST /api/v1/analytics/query                                │
│  GET  /api/v1/metrics/dashboard                              │
└─────────────────────┬────────────────────────────────────────┘
                      │
┌─────────────────────▼────────────────────────────────────────┐
│                Agent Orchestrator                             │
│                  (ReAct Loop)                                 │
│                                                              │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────┐  ┌───────┐│
│  │   Query      │  │    Data      │  │ Analysis │  │Summary││
│  │   Planner    │→ │  Retriever   │→ │  Agent   │→ │ Agent ││
│  │  (Thought)   │  │  (Action)    │  │(Thought) │  │(Final)││
│  └─────────────┘  └──────────────┘  └──────────┘  └───────┘│
└──────────────────────────────────────────────────────────────┘
        │                    │                │
┌───────▼────────┐  ┌───────▼───────┐  ┌─────▼──────────────┐
│  Spring AI     │  │  Spring Data  │  │   Micrometer       │
│  (LLM Client)  │  │  JPA + H2     │  │   + Prometheus     │
└────────────────┘  └───────────────┘  └────────────────────┘
```

## How It Works

1. **User submits a natural language query** via the web dashboard or REST API
2. **QueryPlannerAgent** decomposes the question using Chain-of-Thought prompting
3. **DataRetrieverAgent** translates the plan into JPA queries against contact center data
4. **AnalysisAgent** interprets patterns, trends, and anomalies in the results
5. **SummarizationAgent** generates a concise, executive-level insight
6. **Full trace** of every step is returned with latency and token metrics

### ReAct Pattern

Each agent step follows the **Thought → Action → Observation** cycle:

```
Thought: "I need to find agents with the highest average handle time..."
Action:  query agent_performance WHERE date BETWEEN Feb 17 AND Feb 28
Observation: Sarah Chen: 546s avg, James Wilson: 420s avg, Maria Garcia: 285s avg...

Thought: "Sarah Chen is 60% above the team average of 450s..."
Action:  analyze_trends for Sarah Chen's daily records
Observation: Driven by 8 ESCALATED calls in Billing queue...

Final Answer: "Sarah Chen averaged 8.2 min handle time (team avg: 5.1 min),
              primarily driven by escalation calls in Billing."
```

## Quick Start

### Prerequisites
- Java 17+
- OpenAI API key (optional — app works in fallback mode without it)

### Run
```bash
# Clone
git clone https://github.com/pratushMukherjee/contact-center-ai-analytics.git
cd contact-center-ai-analytics

# Set API key (optional)
export OPENAI_API_KEY=your-key-here

# Build and run (Maven Wrapper included — no Maven install needed)
./mvnw spring-boot:run

# Or on Windows
mvnw.cmd spring-boot:run
```

### Try It

Open **http://localhost:8080** in your browser to use the interactive dashboard — type a question and click **Analyze** to see the full ReAct agent pipeline in action.

Or use the API directly:
```bash
# Health check
curl http://localhost:8080/api/v1/health

# Ask a question
curl -X POST http://localhost:8080/api/v1/analytics/query \
  -H "Content-Type: application/json" \
  -d '{"query": "Which agents had the highest handle time last week?"}'

# View metrics dashboard
curl http://localhost:8080/api/v1/metrics/dashboard

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus | grep ccai

# H2 Database Console
# Open: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:ccai | User: sa | Password: (empty)
```

## Example Queries

| Query | What It Demonstrates |
|-------|---------------------|
| "Which agents had the highest handle time last week?" | Multi-table aggregation, agent ranking |
| "What are the CSAT trends for the Support queue?" | Cross-table joins, trend analysis |
| "Show me the disposition breakdown by queue" | Grouping, percentage calculations |
| "Why are customers unhappy with the Billing team?" | Sentiment analysis, root cause detection |
| "Which queue has the worst service level?" | Queue metrics comparison |

## Tech Stack

| Technology | Purpose |
|-----------|---------|
| Java 17 | Language |
| Spring Boot 3.4 | Application framework |
| Spring AI 1.0 | LLM integration (ChatClient) |
| Spring Data JPA | Data access layer |
| H2 Database | Embedded SQL database |
| Flyway | Database migration management |
| Micrometer + Prometheus | Observability metrics |
| JUnit 5 + Mockito | Testing framework |
| Maven (with wrapper) | Build system |

## Project Structure

```
src/main/java/com/zoom/ccai/
├── agent/
│   ├── orchestrator/     # AgentOrchestrator, ReActLoop
│   ├── planner/          # QueryPlannerAgent (Chain-of-Thought)
│   ├── retriever/        # DataRetrieverAgent (JPA queries)
│   ├── analyzer/         # AnalysisAgent (pattern detection)
│   └── summarizer/       # SummarizationAgent (insight generation)
├── memory/               # AgentMemoryService, ConversationContextManager
├── observability/        # AgentMetrics, TraceInterceptor, EvaluationFramework
├── model/
│   ├── entity/           # JPA entities (CallRecord, AgentPerformance, etc.)
│   └── dto/              # API DTOs (QueryRequest, QueryResponse, AgentStep)
├── repository/           # Spring Data repositories with custom JPQL
├── service/              # AnalyticsService (data aggregation)
├── controller/           # REST API endpoints
└── config/               # AI, Web, Observability configuration
```

## Database Schema

4 tables with realistic contact center data (138 call records, 10 agents, 3 queues):

- **call_records** — Individual call interactions with handle time, sentiment, disposition
- **agent_performance** — Daily agent metrics (AHT, CSAT, FCR, adherence)
- **customer_satisfaction** — CSAT survey responses with free-text comments
- **queue_metrics** — Hourly queue stats (offered, answered, abandoned, SLA)

## Observability

Metrics exposed at `/actuator/prometheus`:

```
ccai_agent_request_count_total          — Total queries processed
ccai_agent_request_duration_seconds     — End-to-end latency (p50, p95, p99)
ccai_agent_step_duration_seconds        — Per-agent latency
ccai_agent_tokens_used                  — LLM token consumption
ccai_agent_llm_latency_seconds          — Raw LLM call latency
ccai_agent_error_count_total            — Error rate tracking
ccai_agent_memory_context_size          — Context window utilization
```

Custom dashboard at `/api/v1/metrics/dashboard` with aggregate stats and recent query log.

## Skills Demonstrated

| Zoom Job Requirement | Implementation |
|---------------------|----------------|
| AI-native architectures (ReAct, Multi-Agent) | 4 specialized agents in ReAct loop with Thought→Action→Observation |
| Java + Spring Boot backend | Full Spring Boot 3.4 application with REST APIs |
| Prompt engineering + context management | Templated system prompts with domain context + token budgeting |
| Agent memory systems | ConversationContextManager with sliding window and archival |
| Observability frameworks | Micrometer/Prometheus metrics + request tracing + evaluation framework |
| SQL database design | 4-table schema with indexes, Flyway migrations, custom JPQL |
| RESTful APIs + microservices | Clean REST endpoints with DTOs, validation, error handling |
| Contact center domain | Realistic CC data: AHT, CSAT, FCR, SLA, sentiment, dispositions |

## License

MIT
