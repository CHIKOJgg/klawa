-- V1__init_schema.sql
-- Инициализация схемы базы данных для AI Assistant Klawa

-- Расширение для генерации UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================
-- 1. ENUMS (Типы данных)
-- ==========================================

-- Роли пользователей
CREATE TYPE user_role AS ENUM ('ROLE_USER', 'ROLE_ADMIN');

-- Статусы задач
CREATE TYPE task_status AS ENUM ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'ARCHIVED');

-- Приоритеты задач
CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');

-- Типы уведомлений
CREATE TYPE notification_type AS ENUM ('TASK_REMINDER', 'AI_MESSAGE', 'SYSTEM_ALERT');

-- Платформы доставки
CREATE TYPE platform_type AS ENUM ('APP', 'TELEGRAM', 'EMAIL');


-- ==========================================
-- 2. AUTH & USERS (Аутентификация)
-- ==========================================

CREATE TABLE IF NOT EXISTS _user (
    id              BIGSERIAL        PRIMARY KEY,
    email           VARCHAR(255)     NOT NULL UNIQUE,
    password       VARCHAR(255)     NOT NULL,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    roles           user_role       NOT NULL DEFAULT 'ROLE_USER',
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ                     -- Soft Delete
);

CREATE TABLE IF NOT EXISTS refresh_token (
    id              BIGSERIAL        PRIMARY KEY,
    token           VARCHAR(512)     NOT NULL UNIQUE,
    user_id         BIGINT          NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
    expires_at      TIMESTAMPTZ     NOT NULL,
    revoked         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_token(user_id);


-- ==========================================
-- 3. CORE (Проекты и Задачи)
-- ==========================================

CREATE TABLE IF NOT EXISTS project (
    id              BIGSERIAL        PRIMARY KEY,
    name            VARCHAR(255)     NOT NULL,
    description     TEXT,
    user_id         BIGINT          NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ                     -- Soft Delete
);

CREATE TABLE IF NOT EXISTS task (
    id              BIGSERIAL        PRIMARY KEY,
    project_id      BIGINT          NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    user_id         BIGINT          NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
    title           VARCHAR(255)     NOT NULL,
    description     TEXT,
    status          task_status     NOT NULL DEFAULT 'OPEN',
    priority        task_priority   NOT NULL DEFAULT 'MEDIUM',
    due_date        TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ                     -- Soft Delete
    
    -- Ограничение: нельзя завершить задачу с прошедшим дедлайном (опционально)
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
    user_id         BIGINT          NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
    type            notification_type NOT NULL,
    message         TEXT            NOT NULL,
    platform        platform_type   NOT NULL DEFAULT 'APP',
    is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    read_at         TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS reminder (
    id              BIGSERIAL        PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
    task_id         BIGINT          REFERENCES task(id) ON DELETE SET NULL,
    trigger_time    TIMESTAMPTZ     NOT NULL,
    message         TEXT            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, FAILED
    last_notified_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Частичный индекс: планировщик ищет только активные напоминания
CREATE INDEX IF NOT EXISTS idx_reminders_trigger ON reminder(trigger_time) WHERE status = 'PENDING';


-- ==========================================
-- 5. AI AGENT & MEMORY (Чат и Память)
-- ==========================================

CREATE TABLE IF NOT EXISTS conversation (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(), -- Безопасный ID для URL
    user_id         BIGINT          NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
    title           VARCHAR(255),

    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ

);

CREATE TABLE IF NOT EXISTS message (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID            NOT NULL REFERENCES conversation(id) ON DELETE CASCADE,
    role            VARCHAR(20)     NOT NULL, -- user, assistant, system
    content         TEXT            NOT NULL,
    token_count     INTEGER,        -- Для контроля стоимости API

    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS memory_entry (
    id              BIGSERIAL        PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
    key             VARCHAR(100)     NOT NULL, -- Например: 'preferred_language'
    value           TEXT            NOT NULL,  -- Например: 'Russian'
    source          VARCHAR(20)     NOT NULL DEFAULT 'MANUAL', -- MANUAL или AI_EXTRACTED

    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, key) -- Один факт на пользователя
);

CREATE INDEX IF NOT EXISTS idx_messages_conv_time ON message(conversation_id, created_at DESC);


-- ==========================================
-- 6. TELEGRAM INTEGRATION
-- ==========================================

CREATE TABLE IF NOT EXISTS telegram_account (
    id              BIGSERIAL        PRIMARY KEY,
    user_id         BIGINT          NOT NULL UNIQUE REFERENCES _user(id) ON DELETE CASCADE,
    telegram_chat_id BIGINT         NOT NULL, -- ID чата в Telegram
    telegram_username VARCHAR(64),
    linked_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
