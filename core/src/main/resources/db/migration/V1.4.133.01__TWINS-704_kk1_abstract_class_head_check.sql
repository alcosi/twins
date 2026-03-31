UPDATE twin_class
SET head_twin_class_id = NULL
WHERE abstract = true AND head_twin_class_id IS NOT NULL;

ALTER TABLE twin_class
    ADD CONSTRAINT chk_twin_class_abstract_head_twin_class
        CHECK (NOT abstract OR head_twin_class_id IS NULL);