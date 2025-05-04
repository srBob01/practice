-- changeset srBob01:007-create-stackoverflow_link-table
CREATE TABLE stackoverflow_link
(
    id          BIGINT PRIMARY KEY,
    question_id TEXT NOT NULL,
    CONSTRAINT fk_stackoverflow_link_id FOREIGN KEY (id) REFERENCES link (id) ON DELETE CASCADE ON UPDATE CASCADE
);
