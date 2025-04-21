CREATE TABLE IF NOT EXISTS twin_attachment_restriction
(
    id                  UUID PRIMARY KEY,
    domain_id           UUID NOT NULL REFERENCES domain (id) ON DELETE CASCADE,
    min_count           INTEGER,
    max_count           INTEGER,
    file_size_mb_limit  INTEGER,
    file_extension_list TEXT,
    file_name_regexp    TEXT
);

ALTER TABLE twin_class
    ADD COLUMN IF NOT EXISTS general_attachment_restriction_id UUID,
    ADD COLUMN IF NOT EXISTS comment_attachment_restriction_id UUID;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM pg_constraint
                       WHERE conname = 'fk_twin_class_general_attachment_restriction') THEN
            ALTER TABLE twin_class
                ADD CONSTRAINT fk_twin_class_general_attachment_restriction
                    FOREIGN KEY (general_attachment_restriction_id)
                        REFERENCES twin_attachment_restriction (id);
        END IF;

        IF NOT EXISTS (SELECT 1
                       FROM pg_constraint
                       WHERE conname = 'fk_twin_class_comment_attachment_restriction') THEN
            ALTER TABLE twin_class
                ADD CONSTRAINT fk_twin_class_comment_attachment_restriction
                    FOREIGN KEY (comment_attachment_restriction_id)
                        REFERENCES twin_attachment_restriction (id);
        END IF;
    END
$$;