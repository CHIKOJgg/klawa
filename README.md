# 🤖 Klawa — Enterprise Modular AI Assistant Platform

[![Java](https://img.shields.io/badge/Java-25-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Modulith](https://img.shields.io/badge/Spring-Modulith-6DB33F?style=flat-square)](https://spring.io/projects/spring-modulith)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Anthropic Claude](https://img.shields.io/badge/AI-Anthropic_Claude_API-CC785C?style=flat-square)](https://www.anthropic.com/)
[![Testcontainers](https://img.shields.io/badge/Testing-Testcontainers-black?style=flat-square)](https://testcontainers.com/)

An enterprise-grade modular monolith platform for task orchestration, contextual long-term memory reasoning, and multi-channel AI interaction.

---

## 🏛 System Architecture (Spring Modulith)

```mermaid
flowchart TD
    subgraph UI ["Client Channels"]
        Web[React / TypeScript Web App]
        TG[Telegram Bot Webhook]
    end

    subgraph Modulith ["Spring Modulith Core (12 Modules)"]
        UserMod[User Module\nJWT Rotation & RBAC]
        TaskMod[Task Management Module\nCRUD & Priorities]
        AgentMod[AI Agent Module\nClaude API + MCP Pipeline]
        RemindMod[Reminder Module\nQuartz / Scheduled]
        NotifyMod[Notification Module\nEmail / Telegram Dispatch]
        EventBus([Spring Modulith ApplicationEvents])
    end

    subgraph Infra ["Infrastructure & Resilience"]
        Resilience[Resilience4j Circuit Breaker]
        Limiter[Bucket4j Rate Limiter]
        PG[(PostgreSQL 16)]
        Redis[(Redis Cache)]
        Prometheus[Actuator + Prometheus]
    end

    UI --> UserMod
    UI --> TaskMod
    UI --> AgentMod
    
    TaskMod -->|TaskCreatedEvent| EventBus
    RemindMod -->|ReminderDueEvent| EventBus
    EventBus --> NotifyMod
    NotifyMod --> TG
    
    AgentMod --> Resilience --> Limiter --> AnthropicAPI([Anthropic Claude API])
    UserMod & TaskMod & AgentMod --> PG
    AgentMod --> Redis
    Prometheus -.-> Modulith
```

---

## ⚡ Key Architectural Highlights

### 1. Modular Monolith with Spring Modulith
- **12 Encapsulated Modules:** `User`, `Task`, `Reminder`, `Notification`, `Agent`, `Telegram`, `Analytics`, etc.
- **Strict Domain Layering:** Each module enforces `api`, `application`, `domain`, and `infrastructure` packages.
- **Architectural Verification:** Validated via `@ApplicationModuleTest` to prevent illegal cross-module dependencies.

### 2. Intelligent Contextual AI Pipeline
- **Anthropic Claude API & MCP:** Dynamic system prompts generated on-the-fly using stored user facts, active tasks, and historical dialogue context.
- **Asynchronous Fact Extraction:** An independent `@Async` background pipeline analyzes incoming conversations to persist long-term memory entries without degrading HTTP response latency.

### 3. Production Hardening & Fault Tolerance
- **Resilience4j Circuit Breaker:** Protects external AI calls with automatic degradation to cached/template fallbacks.
- **Bucket4j Token Bucket:** Enforces strict user rate limiting (e.g. 20 AI calls/hour) to avoid external quota burnout.
- **JWT Token Rotation:** Reusing an invalidated refresh token automatically triggers immediate session revocation across all devices.

### 4. 100% Realistic Integration Testing
- **Testcontainers Engine:** Zero reliance on in-memory H2 databases. Core persistence, relational schemas, and Redis caches are verified against live containerized environments in CI.

---

## 🚀 Quickstart with Docker Compose

```bash
# Clone the repository
git clone https://github.com/CHIKOJgg/klawa.git
cd klawa

# Launch infrastructure and application
docker compose up -d

# Check health metrics
curl http://localhost:8080/actuator/health
```
