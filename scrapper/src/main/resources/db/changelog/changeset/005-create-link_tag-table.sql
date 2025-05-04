-- changeset srBob01:005-create-link_tag-table
CREATE TABLE link_tag
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    chat_link_id BIGINT NOT NULL REFERENCES chat_link (id) ON DELETE CASCADE ON UPDATE CASCADE,
    tag_id       BIGINT NOT NULL REFERENCES tag (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT pk_link_tag UNIQUE (chat_link_id, tag_id)
);

CREATE INDEX idx_link_tag_chat_link_id ON link_tag (chat_link_id);
CREATE INDEX idx_link_tag_tag_id ON link_tag (tag_id);
