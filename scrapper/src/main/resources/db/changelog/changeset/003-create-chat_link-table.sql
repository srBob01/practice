-- changeset srBob01:003-create-chat_link-table
CREATE TABLE chat_link
(
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    chat_id BIGINT NOT NULL REFERENCES chat (id) ON DELETE CASCADE ON UPDATE CASCADE,
    link_id BIGINT NOT NULL REFERENCES link (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT unique_chat_link UNIQUE (chat_id, link_id)
);

CREATE INDEX idx_chat_link_chat_id ON chat_link (chat_id);
CREATE INDEX idx_chat_link_link_id ON chat_link (link_id);
