CREATE TABLE IF NOT EXISTS twin_attachment_restriction
(
    id                  UUID PRIMARY KEY,
    domain_id           UUID NOT NULL,
    min_count           INTEGER,
    max_count           INTEGER,
    file_size_mb_limit  INTEGER,
    file_extension_list TEXT,
    file_name_regexp    TEXT
);

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.table_constraints
                       WHERE constraint_name = 'fk_twin_attachment_restriction_domain'
                         AND table_name = 'twin_attachment_restriction') THEN
            ALTER TABLE twin_attachment_restriction
                ADD CONSTRAINT fk_twin_attachment_restriction_domain
                    FOREIGN KEY (domain_id)
                        REFERENCES domain (id)
                        ON DELETE CASCADE;
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'twin_class'
                         AND column_name = 'general_attachment_restriction_id') THEN
            ALTER TABLE twin_class
                ADD COLUMN general_attachment_restriction_id UUID;
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_name = 'twin_class'
                         AND column_name = 'comment_attachment_restriction_id') THEN
            ALTER TABLE twin_class
                ADD COLUMN comment_attachment_restriction_id UUID;
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.table_constraints
                       WHERE constraint_name = 'fk_twin_class_general_attachment_restriction'
                         AND table_name = 'twin_class') THEN
            ALTER TABLE twin_class
                ADD CONSTRAINT fk_twin_class_general_attachment_restriction
                    FOREIGN KEY (general_attachment_restriction_id)
                        REFERENCES twin_attachment_restriction (id);
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.table_constraints
                       WHERE constraint_name = 'fk_twin_class_comment_attachment_restriction'
                         AND table_name = 'twin_class') THEN
            ALTER TABLE twin_class
                ADD CONSTRAINT fk_twin_class_comment_attachment_restriction
                    FOREIGN KEY (comment_attachment_restriction_id)
                        REFERENCES twin_attachment_restriction (id);
        END IF;
    END
$$;