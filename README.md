# 🤖 AI Assistant Klawa

**Personal AI assistant for task management** — a Spring Boot 4 modular monolith with Telegram integration, Claude AI chat, smart reminders, and productivity analytics.

> **Status:** 🚧 Work in progress. Core infrastructure (auth, database, Docker, integration tests) is complete; several features are still being implemented.

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Quick Start](#-quick-start)
- [Configuration](#-configuration)
- [API Endpoints](#-api-endpoints)
- [Telegram Bot](#-telegram-bot)
- [Testing](#-testing)
- [Project Status](#-project-status)
- [Contributing](#-contributing)

---

## ✨ Features

### 🗂️ Task & Project Management
- CRUD for tasks and projects
- Soft delete support (`deleted_at`)
- Task status state machine: `OPEN → IN_PROGRESS → COMPLETED | ARCHIVED`
- Priority levels: `LOW`, `MEDIUM`, `HIGH`, `URGENT`
- Hierarchy: project → tasks

### 🤖 AI Chat with Claude
- Anthropic Claude API integration (configured; chat wiring in progress)
- Conversation history with context (`conversation` / `message` tables)
- Personalization based on user memory (`memory_entry`)
- Automatic task creation from dialogue (planned)

### ⏰ Smart Reminders
- Scheduled reminder planner (`reminder` table with partial index on `trigger_time`)
- Domain Events for notifications via Spring Modulith
- Statuses: `PENDING`, `SENT`, `FAILED`
- Automatic notification on trigger

### 📊 Productivity Analytics
- JPQL aggregations on tasks
- Project completion statistics
- Metrics via Actuator + Prometheus (`micrometer-registry-prometheus`)

### 🔐 JWT Authentication
- Registration / Login / Refresh Token
- Roles: `ROLE_USER`, `ROLE_ADMIN`
- Access + Refresh tokens (refresh token entity present; rotation logic in progress)
- BCrypt PasswordEncoder

### 📱 Telegram Bot
- Link Telegram account to user
- One-time binding code (deep-link)
- Inline keyboards and commands
- Webhook architecture (bot token configured; webhook handler in progress)

---

## 🛠️ Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Java | 25 |
| **Framework** | Spring Boot | 4.0.6 |
| **Security** | Spring Security + JWT (jjwt) | 0.13.0 |
| **ORM** | Spring Data JPA / Hibernate | — |
| **Database** | PostgreSQL | 17 (Docker) / 16 (Testcontainers) |
| **Migrations** | Flyway | — |
| **AI API** | Anthropic Claude | claude-sonnet-4-20250514 |
| **Modularity** | Spring Modulith | 2.0.6 |
| **Monitoring** | Spring Actuator + Micrometer Prometheus | 1.17.0 |
| **Validation** | Spring Boot Starter Validation | — |
| **Build** | Maven Wrapper | — |
| **Containers** | Docker / Docker Compose | — |
| **Tests** | JUnit 5 + Testcontainers | 2.0.5 |

---

## 🏗️ Architecture

The project is built as a **modular monolith** using Spring Modulith 2.0.6. Modules are declared via `@ApplicationModule(type = OPEN)` on `package-info.java`.

```
src/main/java/org/example/aiassistantklawa/
├── config/                  # Shared config (Security, JWT, Auditing, BaseEntity)
├── user/                    # Users + Auth
│   ├── api/                 # AuthController, AuthService, UserController
│   ├── domain/              # User, Role, RefreshToken
│   └── infrastructure/      # UserRepository, UserService
├── task/                    # Task & Project management
│   ├── api/                 # TaskController, ProjectsController
│   ├── domain/              # Task, Project, TaskStatus, TaskPriority
│   ├── application/usecase/ # SendRequestUseCase, GetRequestUseCase
│   └── infrastructure/      # ProcessingRequestService, RecordRepository
├── reminder/                # Reminders + Scheduler
├── notification/            # Notifications
├── memory/                  # AI chat, messages, memory
├── agent/                   # AI agent configuration
├── telegram/                # Telegram Bot
├── analytics/               # Analytics
├── health/                  # Health checks & metrics
├── shared/                  # Shared components (error handling, request ID filter)
└── exception/               # Error response models
```

### Module layers
- **`api/`** — REST controllers, DTOs (requests/responses)
- **`domain/`** — JPA entities, Value Objects, Domain Events, business logic
- **`infrastructure/`** — Repositories, external service integrations
- **`application/`** — Use cases

### Declared Spring Modulith modules
`@ApplicationModule(type = OPEN)` is declared on: `agent`, `user`, `reminder`, `task`, `shared`. Other packages (`config`, `memory`, `notification`, `telegram`, `analytics`, `health`, `exception`) are not yet declared as modules — see [Project Status](#-project-status).

### Flyway migrations
```
V0__init_tables.sql          → Full schema: users, refresh_token, projects, tasks,
                                reminders, notifications, conversations, messages,
                                memory_entry, telegram_account (179 lines, all enums + indexes)
V1–V4                        → Empty placeholder files
V5__event_publication_table.sql → Spring Modulith event_publication table
V6–V7                        → Columns added to event_publication
```

> **Note:** All real schema lives in `V0`. Migrations `V1`–`V4` are empty. The Modulith `event_publication` table is created by `V5` and extended by `V6`–`V7` (also auto-created by `spring.modulith.events.jdbc.schema-initialization`).

---

## 🚀 Quick Start

### Prerequisites

- **Java 25** (JDK)
- **Docker** and **Docker Compose**
- **PostgreSQL** (only if running without Docker)

### Quick start

```bash
# 1. Clone the repository
git clone https://github.com/CHIKOJgg/klawa.git
cd ai-assistant-klawa

# 2. Configure environment variables
cp .env.example .env
# Edit .env to your needs

# 3. Start PostgreSQL via Docker
docker-compose up -d

# 4. Build and run the application
./mvnw spring-boot:run

# 5. App is available at http://localhost:8080
```

### Full stack via Docker Compose

```bash
docker-compose up --build
```

This starts the app, PostgreSQL 17, and Redis. The app builds from `eclipse-temurin:25-jdk` and runs on `eclipse-temurin:25-jre`.

---

## 🔧 Configuration

Configuration is profile-driven:

| Profile | File | Purpose |
|---------|------|---------|
| `dev` | `application-dev.yaml` | Debug logs, formatted SQL, console trace-id pattern |
| `prod` | `application-prod.yaml` | Warn-level logs, SQL hidden |
| `test` | `application-test.yaml` | Flyway disabled, Hibernate `create-drop`, Testcontainers |

### Environment variables

| Variable | Purpose | Default |
|----------|---------|---------|
| `DB_URL` | PostgreSQL JDBC URL | — (required) |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | — (required) |
| `JWT_SECRET` | JWT signing key (256-bit) | — (required) |
| `JWT_EXPIRATION` | JWT lifetime in ms | `900000` (15 min) |
| `ANTHROPIC_API_KEY` | Anthropic Claude API key | — |
| `ANTHROPIC_MODEL` | Claude model | `claude-sonnet-4-20250514` |
| `TELEGRAM_BOT_TOKEN` | Telegram bot token | — |
| `TELEGRAM_WEBHOOK_URL` | Telegram webhook URL | — |
| `SPRING_PROFILES_ACTIVE` | Active profile | `dev` |

### Actuator endpoints
All web endpoints are exposed except `env`, `beans`, `threaddump`. Health details always shown. Prometheus metrics available at `/actuator/prometheus`. All `/actuator/**` requests are permitted.

---

## 📡 API Endpoints

All endpoints are prefixed with `/api/v1`.

### Auth (`/api/v1/auth`)
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/register` | Register a new user |
| `POST` | `/authenticate` | Log in |

### Tasks (`/api/v1/tasks`)
| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/` | List tasks |
| `GET` | `/{id}` | Get task by ID |
| `POST` | `/` | Create task |
| `PUT` | `/{id}` | Update task |
| `DELETE` | `/{id}` | Delete task |

### Projects, Reminders, Notifications, Memories, Agent, Analytics, Telegram
Similar CRUD endpoints under `/api/v1/projects`, `/api/v1/reminders`, `/api/v1/notifications`, `/api/v1/memory`, `/api/v1/agent`, `/api/v1/analytics`, `/api/v1/telegram`.

### System
| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/system/health` | Health check |
| `GET` | `/api/v1/system/metrics` | System metrics |

> **Note:** OpenAPI / Swagger is not yet integrated. Endpoint details are documented here manually.

---

## 🤖 Telegram Bot

Link your Telegram account to a user:

1. User requests a one-time code via API
2. Sends the code to the bot in Telegram
3. Bot confirms the binding

**Bot commands:** `/start`, `/link <code>`, `/tasks`, `/help`

---

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run a specific test
./mvnw test -Dtest=AuthControllerTest
```

Test database: PostgreSQL 16 (alpine) via Testcontainers, Hibernate DDL: `create-drop`.

Tests included:
- `AiAssistantKlawaApplicationTests` — Spring Modulith structure verification
- `AuthControllerTest` — registration + duplicate-email 409 handling
- `UserRepositoryTest` — Testcontainers JPA base setup

---

## 📊 Project Status

- [x] Authentication (register/login, JWT, refresh token entity)
- [x] Global exception handling with trace IDs
- [x] PostgreSQL schema (Flyway, V0 full schema + Modulith event table)
- [x] Integration tests (Testcontainers)
- [x] Docker / Docker Compose (app + PostgreSQL 17 + Redis)
- [x] Actuator + Prometheus metrics
- [x] Spring Modulith structure verification test
- [ ] Task/Project CRUD with state machine
- [ ] AI chat with Claude (configured, not yet wired)
- [ ] Reminders + Notifications (scheduler)
- [ ] Telegram bot webhook handler
- [ ] Analytics endpoints
- [ ] Refresh token rotation logic
- [ ] Redis integration (configured in compose, no dependency yet)
- [ ] Rate limiting, caching, Circuit Breaker
- [ ] OpenAPI / Swagger
- [ ] CI/CD pipeline
- [ ] Declared `@ApplicationModule` on all packages

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`feature/my-feature`)
3. Commit your changes
4. Open a pull request

Please ensure `./mvnw test` passes before submitting.

---

<div align="center">
  <sub>Built with Java 25 + Spring Boot 4 · AGPL-3.0</sub>
</div>