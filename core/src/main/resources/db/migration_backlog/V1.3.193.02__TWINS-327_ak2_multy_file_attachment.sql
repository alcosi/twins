-- new table creation;
CREATE TABLE if not exists twin_attachment_storage_links (
                                               twin_attachment_id UUID NOT NULL,
                                               link_key VARCHAR(255) NOT NULL,
                                               link_value TEXT,
                                               CONSTRAINT FK_twin_attachment_storage_links_twin_attachment_id
                                                   FOREIGN KEY (twin_attachment_id)
                                                       REFERENCES twin_attachment(id)
                                                       ON DELETE CASCADE,
                                               CONSTRAINT PK_twin_attachment_storage_links
                                                   PRIMARY KEY (twin_attachment_id, link_key)
);
drop index if exists IDX_twin_attachment_storage_links_twin_attachment_id;
CREATE INDEX IDX_twin_attachment_storage_links_twin_attachment_id ON twin_attachment_storage_links(twin_attachment_id);

-- copy current links to map with "origin" key
DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'twin_attachment'
              AND column_name = 'storage_link'
        ) THEN
            INSERT INTO twin_attachment_storage_links (twin_attachment_id, link_key, link_value)
            SELECT
                id AS twin_attachment_id,
                'origin' AS link_key,
                storage_link AS link_value
            FROM twin_attachment
            WHERE storage_link IS NOT NULL
            ON CONFLICT (twin_attachment_id, link_key) DO NOTHING;
        END IF;
    END;
$$;

-- drop unnecessary column after deprecation complete
ALTER TABLE twin_attachment DROP COLUMN if exists storage_link;

CREATE TABLE if not exists draft_twin_attachment_storage_links (
                                                     draft_twin_attachment_id UUID NOT NULL,
                                                     link_key VARCHAR(255) NOT NULL,
                                                     link_value TEXT,
                                                     CONSTRAINT FK_draft_twin_attachment_storage_links_draft_twin_attachment_id
                                                         FOREIGN KEY (draft_twin_attachment_id)
                                                             REFERENCES draft_twin_attachment(id)
                                                             ON DELETE CASCADE,
                                                     CONSTRAINT PK_draft_twin_attachment_storage_links
                                                         PRIMARY KEY (draft_twin_attachment_id, link_key)
);


drop index if exists IDX_draft_twin_attach_storage_links_draft_twin_attachment_id;
CREATE INDEX IDX_draft_twin_attach_storage_links_draft_twin_attachment_id
    ON draft_twin_attachment_storage_links(draft_twin_attachment_id);

ALTER TABLE draft_twin_attachment DROP COLUMN if exists storage_link;
