# 🤖 AI Assistant Klawa

**Personal AI assistant for task management** with Telegram integration and Claude AI support.

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

---

## ✨ Features

### 🗂️ Task & Project Management
- CRUD for tasks and projects
- Soft delete support
- Status model: `OPEN → IN_PROGRESS → COMPLETED | ARCHIVED`
- Priority levels: `LOW`, `MEDIUM`, `HIGH`, `URGENT`
- Hierarchy: project → tasks → subtasks

### 🤖 AI Chat with Claude
- Anthropic Claude API integration
- Conversation history with context
- Personalization based on user memory
- Automatic task creation from dialogue

### ⏰ Smart Reminders
- Scheduled reminder planner
- Domain Events for notifications
- Statuses: `OPEN`, `IN_PROGRESS`, `COMPLETED`, `ARCHIVED`
- Automatic notification on trigger

### 📊 Productivity Analytics
- JPQL aggregations on tasks
- Project completion statistics
- Metrics via Actuator + Prometheus

### 🔐 JWT Authentication
- Registration / Login / Refresh Token
- Roles: `ADMIN`, `USER`
- Access + Refresh tokens
- BCrypt PasswordEncoder

### 📱 Telegram Bot
- Link Telegram account to user
- One-time binding code (deep-link)
- Inline keyboards and commands
- Webhook architecture

---

## 🛠️ Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Java | 25 |
| **Framework** | Spring Boot | 4.0.6 |
| **Security** | Spring Security + JWT (jjwt) | 0.13.0 |
| **ORM** | Spring Data JPA / Hibernate | — |
| **Database** | PostgreSQL | 17 |
| **Migrations** | Flyway | — |
| **AI API** | Anthropic Claude | claude-sonnet-4 |
| **Modularity** | Spring Modulith | 2.0.6 |
| **Monitoring** | Spring Actuator + Micrometer | 1.17.0 |
| **Validation** | Spring Boot Starter Validation | — |
| **Build** | Maven Wrapper | — |
| **Containers** | Docker / Docker Compose | — |
| **Tests** | JUnit 5 + Testcontainers | 2.0.5 |

---

## 🏗️ Architecture

The project is built as a **modular monolith** (Spring Modulith) with clear module separation:

```
src/main/java/org/example/aiassistantklawa/
├── config/                  # Shared config (Security, JWT, Auditing)
├── user/                    # Users + Auth
│   ├── api/                 #  - AuthController
│   ├── domain/              #  - User, Role, RefreshToken
│   └── infrastructure/      #  - UserRepository
├── task/                    # Task management
├── reminder/                # Reminders + Scheduler
├── notification/            # Notifications
├── memory/                  # AI chat, messages, memory
├── agent/                   # AI agent configuration
├── telegram/                # Telegram Bot
├── analytics/               # Analytics
├── health/                  # Health checks & metrics
└── shared/                  # Shared components (error handling)
```

### Module layers
- **`api/`** — REST controllers, DTOs (requests/responses)
- **`domain/`** — JPA entities, Value Objects, Domain Events, business logic
- **`infrastructure/`** — Repositories, external service integrations

### Flyway migrations
```
V1  → create_users_table
V2  → create_tasks_table
V3  → create_projects_table
V4  → create_reminders_table
V5  → create_telegram_accounts
V6  → create_requests
V7  → create_notifications
V8  → create_chat_message
V9  → create_memory
V10 → create_tasks_tasks
```

---

## 🚀 Quick Start

### Prerequisites

- **Java 25** (JDK)
- **Docker** and **Docker Compose**
- **PostgreSQL** (if running without Docker)

### Quick start

```bash
# 1. Clone the repository
git clone https://github.com/your-repo/ai-assistant-klawa.git
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

---

## 🔧 Configuration

| Variable | Purpose | Default |
|----------|---------|---------|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/klawa` |
| `DB_USERNAME` | Database user | `klawa_user` |
| `DB_PASSWORD` | Database password | `changeme` |
| `JWT_SECRET` | JWT signing key (256-bit) | — |
| `JWT_EXPIRATION` | JWT lifetime in ms | `900000` (15 min) |
| `ANTHROPIC_API_KEY` | Anthropic Claude API key | — |
| `ANTHROPIC_MODEL` | Claude model | `claude-sonnet-4-20250514` |
| `TELEGRAM_BOT_TOKEN` | Telegram bot token | — |
| `TELEGRAM_WEBHOOK_URL` | Telegram webhook URL | — |
| `SPRING_PROFILES_ACTIVE` | Active profile | `dev` |

**Profiles:**
- `dev` — development (debug logs, formatted SQL)
- `prod` — production (warn logs)
- `test` — Flyway disabled, DDL auto-create

---

## 📡 API Endpoints

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
Similar CRUD endpoints under `/api/v1/projects`, `/api/v1/reminders`, etc.

### System
| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/system/health` | Health check |
| `GET` | `/api/v1/system/metrics` | System metrics |

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

Test database: PostgreSQL 16 (alpine) via Testcontainers, Hibernate DDL: `create-drop`

---

## 📊 Project Status

- [x] Authentication (register/login, JWT, refresh token)
- [x] Global exception handling with trace IDs
- [x] PostgreSQL schema (Flyway, 10 migrations)
- [x] Integration tests (Testcontainers)
- [ ] Task/Project CRUD with state machine
- [ ] AI chat with Claude
- [ ] Reminders + Notifications
- [ ] Telegram bot webhook
- [ ] Analytics
- [ ] Rate limiting, caching, Circuit Breaker
- [ ] OpenAPI / Swagger
- [ ] CI/CD pipeline

---

<div align="center">
  <sub>Built with Java 25 + Spring Boot 4 · AGPL-3.0</sub>
</div>
