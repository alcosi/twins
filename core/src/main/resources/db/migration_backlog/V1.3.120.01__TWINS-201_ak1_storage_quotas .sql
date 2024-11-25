ALTER TABLE public.twin_attachment ADD COLUMN if not exists size BIGINT NOT NULL DEFAULT 0;

ALTER TABLE public.domain_business_account
    ADD COLUMN if not exists attachments_storage_used_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN if not exists attachments_storage_used_size BIGINT NOT NULL DEFAULT 0;

ALTER TABLE public.domain
    ADD COLUMN if not exists attachments_storage_used_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN if not exists attachments_storage_used_size BIGINT NOT NULL DEFAULT 0;

-- update counters domain_business_account and domain
CREATE OR REPLACE FUNCTION update_attachment_storage()
    RETURNS TRIGGER AS $$
BEGIN
    -- delete attachment
    IF (TG_OP = 'DELETE') THEN
        -- decrease counters for businessAccount
        UPDATE domain_business_account dba
        SET attachments_storage_used_count = attachments_storage_used_count - 1,
            attachments_storage_used_size = attachments_storage_used_size - OLD.size
        FROM twin te WHERE te.id = OLD.twin_id AND dba.id = te.owner_business_account_id;

        -- decrease counters for domain
        UPDATE domain d
        SET attachments_storage_used_count = attachments_storage_used_count - 1,
            attachments_storage_used_size = attachments_storage_used_size - OLD.size
        FROM twin te JOIN twin_class tc ON te.twin_class_id = tc.id WHERE te.id = OLD.twin_id AND d.id = tc.domain_id;

        -- add attachment
    ELSIF (TG_OP = 'INSERT') THEN
        -- increase counters for businessAccount
        UPDATE domain_business_account dba
        SET attachments_storage_used_count = attachments_storage_used_count + 1,
            attachments_storage_used_size = attachments_storage_used_size + NEW.size
        FROM twin te WHERE te.id = NEW.twin_id AND dba.id = te.owner_business_account_id;

        -- increase counters for domain
        UPDATE domain d
        SET attachments_storage_used_count = attachments_storage_used_count + 1,
            attachments_storage_used_size = attachments_storage_used_size + NEW.size
        FROM twin te JOIN twin_class tc ON te.twin_class_id = tc.id WHERE te.id = NEW.twin_id AND d.id = tc.domain_id;


        -- update attachment
    ELSIF (TG_OP = 'UPDATE') THEN
        -- calculate counters for businessAccount
        UPDATE domain_business_account dba
        SET attachments_storage_used_size = attachments_storage_used_size - OLD.size + NEW.size
        FROM twin te WHERE te.id = NEW.twin_id AND dba.id = te.owner_business_account_id;

        -- calculate counters for domain
        UPDATE domain d
        SET attachments_storage_used_size = attachments_storage_used_size - OLD.size + NEW.size
        FROM public.twin te JOIN twin_class tc ON te.twin_class_id = tc.id WHERE te.id = NEW.twin_id AND d.id = tc.domain_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- trigger on twin_attachment table
CREATE TRIGGER trg_update_attachment_storage
    AFTER INSERT OR UPDATE OR DELETE ON twin_attachment
    FOR EACH ROW EXECUTE FUNCTION update_attachment_storage();

