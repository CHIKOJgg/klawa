CREATE TABLE IF NOT EXISTS event_publication (
                                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                 completion_date TIMESTAMP,
                                                 event_type VARCHAR(255) NOT NULL,
                                                 listener_id VARCHAR(255) NOT NULL,
                                                 publication_date TIMESTAMP NOT NULL,
                                                 serialized_event TEXT NOT NULL,
                                                 UNIQUE (listener_id, event_type)
);

CREATE INDEX IF NOT EXISTS idx_event_publication_completion_date ON event_publication(completion_date);