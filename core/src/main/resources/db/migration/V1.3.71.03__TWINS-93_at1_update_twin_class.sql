ALTER TABLE twin_class ALTER COLUMN head_hunter_featurer_id SET DEFAULT 2601;
UPDATE twin_class SET head_hunter_featurer_id = 2601 WHERE head_hunter_featurer_id IS NULL;
