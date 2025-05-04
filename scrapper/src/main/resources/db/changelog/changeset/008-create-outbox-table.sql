-- changeset srBob01:008-create-outbox-table
CREATE TABLE outbox
(
    id           BIGSERIAL PRIMARY KEY,
    topic        VARCHAR(255) NOT NULL,
    payload      TEXT        NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

CREATE INDEX idx_outbox_processed_at ON outbox (processed_at);
