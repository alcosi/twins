alter table public.twin_alias
    add if not exists archived boolean default false not null;


CREATE OR REPLACE FUNCTION alias_archive_existing()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE twin_alias
    SET archived = TRUE
    WHERE twin_id = NEW.twin_id
      AND twin_alias_type_id = NEW.twin_alias_type_id
      AND archived = FALSE;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_archive_existing_aliases
    BEFORE INSERT ON twin_alias
    FOR EACH ROW
EXECUTE FUNCTION alias_archive_existing();
