INSERT INTO featurer_type (id, name, description)
VALUES (57, 'TwinSearchExecutor', 'Executes one saved twin search')
ON CONFLICT (id) DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (5701, 57, '', '', '', false),
       (5702, 57, '', '', '', false)
ON CONFLICT DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (2724, 27, '', 'By field date (requested)', 'Adds a date/timestamp field condition with bounds read from named request params', false),
       (2725, 27, '', 'By fields not null (given)', 'Adds one OR-clause: at least one of the given date/timestamp fields has a value', false),
       (2726, 27, '', 'By direct children count (given)', 'Bounds the twin direct children counter with an inclusive range', false)
ON CONFLICT DO NOTHING;

ALTER TABLE twin_search ADD COLUMN IF NOT EXISTS twin_search_executor_featurer_id integer DEFAULT 5701;
ALTER TABLE twin_search ADD COLUMN IF NOT EXISTS twin_search_executor_params hstore;

UPDATE twin_search
SET twin_search_executor_featurer_id = 5701
WHERE twin_search_executor_featurer_id IS NULL;

ALTER TABLE twin_search ALTER COLUMN twin_search_executor_featurer_id SET NOT NULL;

ALTER TABLE twin_search DROP CONSTRAINT IF EXISTS twin_search_twin_search_executor_featurer_id_fk;
ALTER TABLE twin_search
    ADD CONSTRAINT twin_search_twin_search_executor_featurer_id_fk
        FOREIGN KEY (twin_search_executor_featurer_id)
            REFERENCES featurer (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS twin_search_twin_search_executor_featurer_id_idx
    ON twin_search (twin_search_executor_featurer_id);
