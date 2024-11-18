-- rename column name because current name is not correct
DO $$
BEGIN
		IF NOT EXISTS (
			SELECT 1
			FROM information_schema.columns
			WHERE table_name = 'twinflow_schema' AND column_name = 'description'
		) THEN
ALTER TABLE twinflow_schema
    RENAME COLUMN descrption TO description;
END IF;
END $$;
