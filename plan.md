# Contact Center AI Analytics Engine - Implementation Plan

## Context

**Target Role:** Zoom Software Developer Engineer Intern - Contact Center AI Analytics Team (R18638, deadline March 6, 2026)

**Why this project:** The Zoom team builds AI-powered analytics for contact centers using multi-agent systems, ReAct patterns, Java/Spring Boot, and LLM orchestration. This project demonstrates every listed skill by building a working prototype of exactly what the team ships.

**What it does:** A user asks a natural language question like *"Which agents had the highest average handle time last week?"* and a multi-agent system powered by ReAct reasoning plans a query, fetches data, analyzes trends, and returns actionable insights - with full observability.

---

## Phase 1: Project Scaffolding & Git Setup
**Commit: "Phase 1: Initialize Spring Boot project with Maven and project structure"**

### Project Structure
```
contact-center-ai-analytics/
├── pom.xml
├── README.md
├── .gitignore
├── .env.example
├── docker-compose.yml
└── src/
    ├── main/
    │   ├── java/com/zoom/ccai/
    │   │   ├── CcaiApplication.java
    │   │   ├── config/
    │   │   │   ├── AiConfig.java
    │   │   │   ├── ObservabilityConfig.java
    │   │   │   └── WebConfig.java
    │   │   ├── agent/
    │   │   │   ├── orchestrator/
    │   │   │   │   ├── AgentOrchestrator.java
    │   │   │   │   └── ReActLoop.java
    │   │   │   ├── planner/
    │   │   │   │   └── QueryPlannerAgent.java
    │   │   │   ├── retriever/
    │   │   │   │   └── DataRetrieverAgent.java
    │   │   │   ├── analyzer/
    │   │   │   │   └── AnalysisAgent.java
    │   │   │   └── summarizer/
    │   │   │       └── SummarizationAgent.java
    │   │   ├── memory/
    │   │   │   ├── AgentMemoryService.java
    │   │   │   └── ConversationContextManager.java
    │   │   ├── observability/
    │   │   │   ├── AgentMetrics.java
    │   │   │   ├── TraceInterceptor.java
    │   │   │   └── EvaluationFramework.java
    │   │   ├── model/
    │   │   │   ├── entity/
    │   │   │   │   ├── CallRecord.java
    │   │   │   │   ├── AgentPerformance.java
    │   │   │   │   ├── CustomerSatisfaction.java
    │   │   │   │   └── QueueMetric.java
    │   │   │   └── dto/
    │   │   │       ├── QueryRequest.java
    │   │   │       ├── QueryResponse.java
    │   │   │       ├── AgentStep.java
    │   │   │       └── InsightResult.java
    │   │   ├── repository/
    │   │   │   ├── CallRecordRepository.java
    │   │   │   ├── AgentPerformanceRepository.java
    │   │   │   ├── CustomerSatisfactionRepository.java
    │   │   │   └── QueueMetricRepository.java
    │   │   ├── service/
    │   │   │   ├── AnalyticsService.java
    │   │   │   └── DataIngestionService.java
    │   │   └── controller/
    │   │       ├── AnalyticsController.java
    │   │       └── HealthController.java
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── prompts/
    │       │   ├── planner-system.st
    │       │   ├── retriever-system.st
    │       │   ├── analyzer-system.st
    │       │   └── summarizer-system.st
    │       └── db/migration/
    │           ├── V1__create_call_records.sql
    │           ├── V2__create_agent_performance.sql
    │           ├── V3__create_customer_satisfaction.sql
    │           └── V4__seed_sample_data.sql
    └── test/
        └── java/com/zoom/ccai/
            ├── CcaiApplicationTests.java
            ├── agent/AgentOrchestratorTest.java
            ├── agent/ReActLoopTest.java
            └── controller/AnalyticsControllerTest.java
```

### Tech Stack
- **Java 17** + **Spring Boot 3.4**
- **Spring AI 1.0.0** (OpenAI/Anthropic chat client integration)
- **H2 Database** (embedded, zero-config for demo) + Flyway migrations
- **Micrometer + Prometheus** for observability metrics
- **Maven** build system
- **JUnit 5 + MockMvc** for testing

### Key Dependencies (pom.xml)
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-ai-openai-spring-boot-starter`
- `spring-boot-starter-actuator`
- `micrometer-registry-prometheus`
- `h2` (runtime)
- `flyway-core`
- `lombok`

---

## Phase 2: Data Layer - Entities, Repositories, Schema
**Commit: "Phase 2: Add data layer with entities, repositories, and seed data"**

### Database Schema (H2 with Flyway migrations)

**call_records** - Individual call interactions
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT PK | Auto-increment |
| call_id | VARCHAR(36) | Unique call identifier |
| agent_name | VARCHAR(100) | Contact center agent name |
| customer_id | VARCHAR(36) | Customer identifier |
| queue_name | VARCHAR(50) | Queue (Sales, Support, Billing) |
| start_time | TIMESTAMP | Call start |
| end_time | TIMESTAMP | Call end |
| handle_time_seconds | INT | Total handle time |
| hold_time_seconds | INT | Time on hold |
| wrap_up_seconds | INT | After-call work time |
| disposition | VARCHAR(30) | RESOLVED, TRANSFERRED, ESCALATED, ABANDONED |
| sentiment_score | DOUBLE | -1.0 to 1.0 |
| topic | VARCHAR(100) | Call topic/category |

**agent_performance** - Daily agent metrics
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT PK | |
| agent_name | VARCHAR(100) | |
| date | DATE | |
| calls_handled | INT | |
| avg_handle_time | DOUBLE | |
| avg_csat | DOUBLE | 1-5 scale |
| first_call_resolution_rate | DOUBLE | 0-1 |
| adherence_rate | DOUBLE | Schedule adherence 0-1 |

**customer_satisfaction** - CSAT survey results
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT PK | |
| call_id | VARCHAR(36) | FK to call_records |
| score | INT | 1-5 |
| comment | TEXT | Free text feedback |
| survey_date | TIMESTAMP | |

**queue_metrics** - Queue-level hourly stats
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT PK | |
| queue_name | VARCHAR(50) | |
| timestamp_hour | TIMESTAMP | Hourly bucket |
| calls_offered | INT | |
| calls_answered | INT | |
| calls_abandoned | INT | |
| avg_wait_seconds | INT | |
| service_level_pct | DOUBLE | % answered within SLA |

### Seed Data
- V4 migration inserts ~500 realistic call records across 2 weeks
- 10 agents across 3 queues (Sales, Support, Billing)
- Realistic distributions for handle times, CSAT scores, sentiment

---

## Phase 3: AI Agent Architecture (ReAct Multi-Agent System)
**Commit: "Phase 3: Implement ReAct multi-agent orchestration with 4 specialized agents"**

### Core Architecture

```
User Query
    │
    ▼
┌─────────────────────┐
│  AgentOrchestrator   │  ← Central coordinator
│  (ReAct Loop)        │
└──────┬──────────────┘
       │
       ├──→ QueryPlannerAgent    → Breaks NL query into structured plan
       │      (Thought → Action: decompose question)
       │
       ├──→ DataRetrieverAgent   → Executes SQL via Spring Data
       │      (Action: fetch data → Observation: raw results)
       │
       ├──→ AnalysisAgent        → Statistical analysis & trend detection
       │      (Thought: what patterns? → Action: compute)
       │
       └──→ SummarizationAgent   → Human-readable insight generation
              (Action: synthesize → Final Answer)
```

### ReAct Pattern Implementation

The `ReActLoop.java` implements the Reasoning + Acting pattern:

```java
public class ReActLoop {
    // Each iteration: Thought → Action → Observation
    // Max iterations: 5 (prevent infinite loops)
    // Each step is traced with latency + token metrics

    public QueryResponse execute(String userQuery, String conversationId) {
        List<AgentStep> steps = new ArrayList<>();
        String context = userQuery;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            // THOUGHT: Planner decides what to do next
            ThoughtResult thought = plannerAgent.think(context, steps);

            if (thought.isFinalAnswer()) {
                return buildResponse(thought, steps);
            }

            // ACTION: Execute the chosen action
            ActionResult action = executeAction(thought.getAction(), thought.getParams());

            // OBSERVATION: Record what happened
            steps.add(new AgentStep(thought, action));
            context = updateContext(context, steps);
        }
    }
}
```

### Agent Descriptions

1. **QueryPlannerAgent** - Receives NL query, outputs a structured plan with steps. Uses Chain-of-Thought prompting to decompose complex questions. Returns JSON with: `{steps: [{action: "query_data", params: {...}}, ...]}`

2. **DataRetrieverAgent** - Takes structured query parameters, builds Spring Data JPA queries dynamically. Uses `@Query` annotations and Specification API for flexible filtering. Returns raw data results.

3. **AnalysisAgent** - Receives raw data, performs computations (averages, trends, comparisons, anomaly detection). Uses the LLM to interpret patterns and identify insights from the numbers.

4. **SummarizationAgent** - Takes analysis results + original question, generates a clear natural language response with key metrics highlighted and actionable recommendations.

### Prompt Templates (StringTemplate .st files)
Each agent has a dedicated system prompt in `src/main/resources/prompts/` with:
- Role definition
- Available tools/actions
- Output format specification
- Few-shot examples
- Context window management (only relevant history passed)

---

## Phase 4: Memory System & Context Management
**Commit: "Phase 4: Add agent memory system with conversation context management"**

### Memory Architecture

```java
@Service
public class AgentMemoryService {
    // Short-term: Current conversation steps (in-memory ConcurrentHashMap)
    // Long-term: Conversation summaries (H2 table)

    private final Map<String, ConversationContext> activeContexts;

    // Sliding window: Keep last N steps to manage token budget
    // Summarize older steps into a compact context
    public String getContextWindow(String conversationId, int maxTokens);

    // Store completed conversation summary for future reference
    public void archiveConversation(String conversationId);
}
```

### ConversationContextManager
- Tracks per-conversation state: user query, agent steps, intermediate results
- Implements token budgeting: estimates token count, trims oldest context
- Provides conversation continuity: "What about last month's data?" works as follow-up

---

## Phase 5: Observability & Evaluation Framework
**Commit: "Phase 5: Add observability with Micrometer metrics and evaluation framework"**

### Metrics Collected (via Micrometer)
```
ccai.agent.request.count         - Total requests (tagged by status)
ccai.agent.request.duration      - End-to-end latency histogram
ccai.agent.step.duration         - Per-agent-step latency
ccai.agent.step.count            - Steps per query (ReAct iterations)
ccai.agent.tokens.used           - Token consumption per request
ccai.agent.llm.latency           - Raw LLM call latency
ccai.agent.error.count           - Errors by type
ccai.agent.memory.context.size   - Context window utilization
```

### Evaluation Framework
```java
@Component
public class EvaluationFramework {
    // Measures: relevance, correctness, completeness
    // Logs each query with: input, output, steps, latency, token count
    // Provides /api/v1/evaluations endpoint for review
}
```

### Endpoints
- `GET /actuator/prometheus` - Prometheus-format metrics
- `GET /actuator/health` - Health check with DB and AI provider status
- `GET /api/v1/metrics/dashboard` - Custom JSON summary of agent metrics

---

## Phase 6: REST API & Controller Layer
**Commit: "Phase 6: Add REST API endpoints with request/response DTOs"**

### API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/analytics/query` | Submit NL query, get AI-powered insight |
| GET | `/api/v1/analytics/query/{id}` | Get query result by ID |
| GET | `/api/v1/analytics/history` | List past queries for a session |
| GET | `/api/v1/metrics/dashboard` | Agent performance metrics dashboard |
| GET | `/api/v1/health` | System health status |

### Request/Response Examples

**POST /api/v1/analytics/query**
```json
// Request
{
  "query": "Which agents had the highest handle time last week and what caused it?",
  "conversationId": "optional-for-followup"
}

// Response
{
  "id": "q-abc123",
  "query": "Which agents had the highest handle time...",
  "insight": "Agent Sarah Chen averaged 8.2 min handle time last week (team avg: 5.1 min), primarily driven by 12 escalation calls in the Billing queue. Recommendation: Review billing escalation procedures.",
  "steps": [
    {"agent": "QueryPlanner", "thought": "Need to find agents with highest AHT...", "durationMs": 230},
    {"agent": "DataRetriever", "thought": "Querying agent_performance for last 7 days...", "durationMs": 45},
    {"agent": "Analyzer", "thought": "Sarah Chen is 60% above team average...", "durationMs": 310},
    {"agent": "Summarizer", "thought": "Synthesizing findings...", "durationMs": 280}
  ],
  "metadata": {
    "totalDurationMs": 865,
    "tokensUsed": 1847,
    "reactIterations": 4,
    "model": "gpt-4o-mini"
  }
}
```

---

## Phase 7: Tests & Documentation
**Commit: "Phase 7: Add unit tests, integration tests, and README"**

### Tests
- `AgentOrchestratorTest` - Tests ReAct loop with mocked LLM responses
- `ReActLoopTest` - Tests iteration limits, error handling, step tracking
- `AnalyticsControllerTest` - MockMvc tests for all endpoints
- `CcaiApplicationTests` - Context loads test

### README.md Content
- Project overview with architecture diagram (ASCII)
- How it maps to Zoom Contact Center's AI analytics
- Quick start guide (`mvn spring-boot:run`)
- Example queries to try
- Architecture deep-dive
- Observability section with Prometheus/Grafana screenshots description
- Skills demonstrated checklist mapped to job requirements

---

## Phase 8: Polish & Final Commit
**Commit: "Phase 8: Final polish - docker-compose, .env.example, and demo queries"**

- `docker-compose.yml` with optional PostgreSQL for production-like setup
- `.env.example` with `OPENAI_API_KEY=your-key-here`
- Pre-loaded demo queries in README
- Final code cleanup

---

## Implementation Order & Git Strategy

Each phase gets its own commit to GitHub, showing clean development progression:

```
git init
git remote add origin <github-repo-url>

Phase 1 → commit + push  (scaffolding)
Phase 2 → commit + push  (data layer)
Phase 3 → commit + push  (AI agents - the star of the show)
Phase 4 → commit + push  (memory system)
Phase 5 → commit + push  (observability)
Phase 6 → commit + push  (REST API)
Phase 7 → commit + push  (tests + docs)
Phase 8 → commit + push  (polish)
```

Also save `plan.md` in project root at the start.

---

## Why This Project Guarantees an Interview

| Job Requirement | How This Project Demonstrates It |
|----------------|----------------------------------|
| ReAct, Multi-Agent Systems | 4 specialized agents in ReAct loop |
| Java + Spring Boot | Full Spring Boot 3.4 application |
| Prompt engineering + context management | Templated prompts + token budgeting |
| Agent memory systems | ConversationContextManager with sliding window |
| Observability frameworks | Micrometer + Prometheus + custom eval |
| SQL database design | 4-table schema with Flyway migrations |
| RESTful APIs | Clean REST endpoints with DTOs |
| Contact center domain | Realistic CC data: AHT, CSAT, queues, FCR |

---

## Verification Plan

1. `mvn clean install` - All tests pass
2. `mvn spring-boot:run` - App starts on port 8080
3. `curl localhost:8080/api/v1/health` - Returns healthy
4. `curl -X POST localhost:8080/api/v1/analytics/query -H "Content-Type: application/json" -d '{"query":"What is the average handle time by queue?"}'` - Returns AI insight with ReAct steps
5. `curl localhost:8080/actuator/prometheus` - Shows agent metrics
6. H2 console at `localhost:8080/h2-console` - Verify seed data loaded
