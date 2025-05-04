-- changeset srBob01:006-create-github_link-table
CREATE TABLE github_link
(
    id          BIGINT PRIMARY KEY,
    owner       TEXT        NOT NULL,
    repo        TEXT        NOT NULL,
    item_number TEXT,
    event_type  VARCHAR(50) NOT NULL,
    CONSTRAINT fk_github_link_id FOREIGN KEY (id) REFERENCES link (id) ON DELETE CASCADE ON UPDATE CASCADE
);
