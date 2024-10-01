DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'twin_status' AND column_name = 'background_color'
    ) THEN
ALTER TABLE twin_status
    RENAME COLUMN color TO background_color;
END IF;
END $$;

alter table twin_status
    add if not exists font_color varchar;