UPDATE twin_comment
SET created_at = NOW()
WHERE created_at IS NULL;

ALTER TABLE twin_comment
    ALTER COLUMN created_at SET NOT NULL;