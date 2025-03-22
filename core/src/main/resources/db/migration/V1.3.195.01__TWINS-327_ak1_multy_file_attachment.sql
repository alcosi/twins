drop table if exists public.draft_twin_attachment_modification_links;
drop table if exists public.twin_attachment_modification_links;

CREATE TABLE IF NOT EXISTS twin_attachment_modification (
                                                            id UUID PRIMARY KEY,
                                                            twin_attachment_id UUID NOT NULL,
                                                            modification_type VARCHAR NOT NULL,
                                                            storage_file_key VARCHAR,
                                                            CONSTRAINT UK_twin_attachment_modification
                                                                UNIQUE (twin_attachment_id, modification_type),
                                                            CONSTRAINT FK_twin_attachment_modification_twin_attachment
                                                                FOREIGN KEY (twin_attachment_id)
                                                                    REFERENCES twin_attachment(id)
                                                                    ON DELETE CASCADE
);

alter table draft_twin_attachment add column if not exists modifications varchar null;

DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'draft_twin_attachment'
              AND column_name = 'storage_link'
        ) THEN
            ALTER TABLE public.draft_twin_attachment
                RENAME COLUMN storage_link TO storage_file_key;
        END IF;
    END;
$$;

DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'twin_attachment'
              AND column_name = 'storage_link'
        ) THEN
            ALTER TABLE public.twin_attachment
                RENAME COLUMN storage_link TO storage_file_key;
        END IF;
    END;
$$;
