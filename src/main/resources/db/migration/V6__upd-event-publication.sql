-- Добавить колонку completion_attempts
ALTER TABLE event_publication ADD COLUMN IF NOT EXISTS completion_attempts INTEGER DEFAULT 0;

-- Добавить колонку completion_status (если требуется)
ALTER TABLE event_publication ADD COLUMN IF NOT EXISTS completion_status VARCHAR(255);

-- Создать индекс для производительности (опционально)
CREATE INDEX IF NOT EXISTS idx_event_publication_completion_status ON event_publication(completion_status);