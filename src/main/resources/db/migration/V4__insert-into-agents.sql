-- Добавь в V4__agent_memory.sql

CREATE TABLE IF NOT EXISTS conversations (
                                             id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(), -- UUID безопаснее для публичных ID
                                             title      VARCHAR(255),
                                             user_id    BIGINT       NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
                                             created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS messages (
                                        id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                        conversation_id UUID        NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
                                        role            VARCHAR(20) NOT NULL, -- 'user', 'assistant' или 'system'
                                        content         TEXT        NOT NULL,
                                        token_count     INTEGER,              -- Для подсчета стоимости запросов к Anthropic
                                        created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS memory_entries (
                                              id         BIGSERIAL    PRIMARY KEY,
                                              user_id    BIGINT       NOT NULL REFERENCES _user(id) ON DELETE CASCADE,
                                              key        VARCHAR(100) NOT NULL, -- Например: 'preferred_language'
                                              value      TEXT         NOT NULL, -- Например: 'Russian'
                                              source     VARCHAR(20)  NOT NULL DEFAULT 'MANUAL', -- 'MANUAL' или 'AI_EXTRACTED'
                                              created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                                              updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                                              UNIQUE (user_id, key) -- Один ключ на пользователя
);

-- Индексы для производительности чата и поиска памяти
CREATE INDEX IF NOT EXISTS idx_messages_conv_time ON messages(conversation_id, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_memory_user        ON memory_entries(user_id);