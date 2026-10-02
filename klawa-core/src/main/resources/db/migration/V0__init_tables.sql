-- V0__init_tables.sql
-- Инициализация схемы базы данных для AI Assistant Klawa (Keycloak Identity Provider)

-- Расширение для генерации UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================
-- 1. ENUMS (Типы данных)
-- ==========================================

-- Статусы задач
CREATE TYPE task_status AS ENUM ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'ARCHIVED');

-- Приоритеты задач
CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');

-- Типы уведомлений
CREATE TYPE notification_type AS ENUM ('TASK_REMINDER', 'AI_MESSAGE', 'SYSTEM_ALERT');

-- Платформы доставки
CREATE TYPE platform_type AS ENUM ('APP', 'TELEGRAM', 'EMAIL');


-- ==========================================
-- 2. USER PROFILE (Профиль пользователя из Keycloak)
-- Аутентификация, пароли, роли и refresh-токены хранятся в Keycloak.
-- Здесь сохраняется локальный профиль с id = Keycloak sub (UUID).
-- ==========================================

CREATE TABLE IF NOT EXISTS user_profile (
    id              UUID             PRIMARY KEY, -- sub из Keycloak JWT
    email           VARCHAR(255)     NOT NULL UNIQUE,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    is_active       BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ                      -- Soft Delete
);


-- ==========================================
-- 3. CORE (Проекты и Задачи)
-- ==========================================

CREATE TABLE IF NOT EXISTS project (
    id              BIGSERIAL        PRIMARY KEY,
    name            VARCHAR(255)     NOT NULL,
    description     TEXT,
    user_id         UUID             NOT NULL REFERENCES user_profile(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ                      -- Soft Delete
);

CREATE INDEX IF NOT EXISTS idx_projects_user ON project(user_id);

CREATE TABLE IF NOT EXISTS task (
    id              BIGSERIAL        PRIMARY KEY,
    project_id      BIGINT           NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    user_id         UUID             NOT NULL REFERENCES user_profile(id) ON DELETE CASCADE,
    title           VARCHAR(255)     NOT NULL,
    description     TEXT,
    status          task_status      NOT NULL DEFAULT 'OPEN',
    priority        task_priority    NOT NULL DEFAULT 'MEDIUM',
    due_date        TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,                     -- Soft Delete
    
    CONSTRAINT chk_task_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'ARCHIVED'))
);

-- Индексы для производительности
CREATE INDEX IF NOT EXISTS idx_tasks_user_status ON task(user_id, status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_tasks_project     ON task(project_id);


-- ==========================================
-- 4. REMINDERS & NOTIFICATIONS (Напоминания)
-- ==========================================

CREATE TABLE IF NOT EXISTS notification (
    id              BIGSERIAL        PRIMARY KEY,
    user_id         UUID             NOT NULL REFERENCES user_profile(id) ON DELETE CASCADE,
    type            notification_type NOT NULL,
    message         TEXT             NOT NULL,
    platform        platform_type    NOT NULL DEFAULT 'APP',
    is_read         BOOLEAN          NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    read_at         TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_notifications_user ON notification(user_id, is_read);

CREATE TABLE IF NOT EXISTS reminder (
    id              BIGSERIAL        PRIMARY KEY,
    user_id         UUID             NOT NULL REFERENCES user_profile(id) ON DELETE CASCADE,
    task_id         BIGINT           REFERENCES task(id) ON DELETE SET NULL,
    trigger_time    TIMESTAMPTZ      NOT NULL,
    message         TEXT             NOT NULL,
    status          VARCHAR(20)      NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, FAILED
    last_notified_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);

-- Частичный индекс: планировщик ищет только активные напоминания
CREATE INDEX IF NOT EXISTS idx_reminders_trigger ON reminder(trigger_time) WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_reminders_user    ON reminder(user_id);


-- ==========================================
-- 5. SPRING MODULITH (Transactional Outbox)
-- ==========================================

CREATE TABLE IF NOT EXISTS event_publication (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    completion_date         TIMESTAMP,
    event_type              VARCHAR(255) NOT NULL,
    listener_id             VARCHAR(255) NOT NULL,
    publication_date        TIMESTAMP NOT NULL,
    serialized_event        TEXT NOT NULL,
    completion_attempts     INTEGER DEFAULT 0,
    completion_status       VARCHAR(255),
    last_resubmission_date  TIMESTAMP,
    UNIQUE (listener_id, event_type)
);

CREATE INDEX IF NOT EXISTS idx_event_publication_completion_date ON event_publication(completion_date);
CREATE INDEX IF NOT EXISTS idx_event_publication_completion_status ON event_publication(completion_status);
CREATE INDEX IF NOT EXISTS idx_event_publication_last_resubmission_date ON event_publication(last_resubmission_date);


-- ==========================================
-- 6. AI AGENT & MEMORY (Чат и Память)
-- ==========================================

CREATE TABLE IF NOT EXISTS conversation (
    id              UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID             NOT NULL REFERENCES user_profile(id) ON DELETE CASCADE,
    title           VARCHAR(255),
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_conversations_user ON conversation(user_id);

CREATE TABLE IF NOT EXISTS message (
    id              UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID             NOT NULL REFERENCES conversation(id) ON DELETE CASCADE,
    role            VARCHAR(20)      NOT NULL, -- USER, ASSISTANT, SYSTEM
    content         TEXT             NOT NULL,
    token_count     INTEGER,                   -- Для контроля стоимости API
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_messages_conv_time ON message(conversation_id, created_at DESC);

CREATE TABLE IF NOT EXISTS memory_entry (
    id              BIGSERIAL        PRIMARY KEY,
    user_id         UUID             NOT NULL REFERENCES user_profile(id) ON DELETE CASCADE,
    key             VARCHAR(100)     NOT NULL, -- 'preferred_language', 'working_hours' и т.д.
    value           TEXT             NOT NULL,
    source          VARCHAR(20)      NOT NULL DEFAULT 'MANUAL', -- MANUAL или AI_EXTRACTED
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, key)                       -- Один ключ на пользователя
);

CREATE INDEX IF NOT EXISTS idx_memory_user ON memory_entry(user_id);


-- ==========================================
-- 7. TELEGRAM INTEGRATION
-- ==========================================

CREATE TABLE IF NOT EXISTS telegram_account (
    id                  BIGSERIAL        PRIMARY KEY,
    user_id             UUID             NOT NULL UNIQUE REFERENCES user_profile(id) ON DELETE CASCADE,
    telegram_chat_id    BIGINT           NOT NULL,
    telegram_username   VARCHAR(64),
    linked_at           TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_telegram_chat ON telegram_account(telegram_chat_id);
