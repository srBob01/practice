-- changeset srBob01:002-create-link-table
CREATE TABLE link
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    original_url  TEXT UNIQUE                         NOT NULL,
    last_checked  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_modified TIMESTAMP                           NOT NULL,
    version       BIGINT    DEFAULT 0                 NOT NULL,
    type          VARCHAR(50)                         NOT NULL
);
