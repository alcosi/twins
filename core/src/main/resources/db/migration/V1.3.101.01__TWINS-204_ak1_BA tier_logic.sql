CREATE TABLE  if not exists public.tier (
            id uuid NOT NULL,
            domain_id uuid,
            name character varying,
            custom boolean DEFAULT false,
            permission_schema_id uuid,
            twinflow_schema_id uuid,
            twin_class_schema_id uuid,
            attachments_storage_quota_count integer NOT NULL default 0,
            attachments_storage_quota_size bigint NOT NULL default 0,
            user_count_quota integer NOT NULL default 0,
            description character varying
);
alter table public.domain drop constraint if exists domain_tier_id_fk;
alter table public.domain_business_account drop constraint if exists domain_business_account_tier_id_fk;
alter table public.tier drop constraint if exists tier_pk;
alter table public.tier drop constraint if exists tier_domain_id_fk;
alter table public.tier drop constraint if exists tier_permission_schema_id_fk;
alter table public.tier drop constraint if exists tier_twinflow_schema_id_fk;
alter table public.tier drop constraint if exists tier_twin_class_schema_id_fk;

ALTER TABLE ONLY public.tier ADD CONSTRAINT tier_pk PRIMARY KEY (id);
ALTER TABLE ONLY public.tier ADD CONSTRAINT tier_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id);
ALTER TABLE ONLY public.tier ADD CONSTRAINT tier_permission_schema_id_fk FOREIGN KEY (permission_schema_id) REFERENCES public.permission_schema(id);
ALTER TABLE ONLY public.tier ADD CONSTRAINT tier_twinflow_schema_id_fk FOREIGN KEY (twinflow_schema_id) REFERENCES public.twinflow_schema(id);
ALTER TABLE ONLY public.tier ADD CONSTRAINT tier_twin_class_schema_id_fk FOREIGN KEY (twin_class_schema_id) REFERENCES public.twin_class_schema(id);

alter table public.domain add column if not exists default_tier_id uuid;
ALTER TABLE ONLY public.domain ADD CONSTRAINT domain_tier_id_fk FOREIGN KEY (default_tier_id) REFERENCES public.tier(id);

alter table public.domain_business_account add column if not exists tier_id uuid;
ALTER TABLE ONLY public.domain_business_account ADD CONSTRAINT domain_business_account_tier_id_fk FOREIGN KEY (tier_id) REFERENCES public.tier(id);



DROP TRIGGER IF EXISTS tiers_tier_update_trigger ON public.tier;
DROP TRIGGER IF EXISTS tiers_domain_business_account_tier_id_update_trigger ON public.domain_business_account;
DROP FUNCTION IF EXISTS public.tiers_update_business_account_properties_on_tier_change();
DROP FUNCTION IF EXISTS public.tiers_update_business_account_properties_on_self_tier_id_change();

-- stored function for update records in domain_business_account for all BA which used changed tier.
CREATE OR REPLACE FUNCTION tiers_update_business_account_properties_on_tier_change() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.custom THEN
        RETURN NULL; --if custom changed to true then return. Tier properties will apply to BA, only if this tier selected on BA create or update
    END IF;

    IF OLD.permission_schema_id IS DISTINCT FROM NEW.permission_schema_id OR
       OLD.twinflow_schema_id IS DISTINCT FROM NEW.twinflow_schema_id OR
       OLD.twin_class_schema_id IS DISTINCT FROM NEW.twin_class_schema_id OR
       OLD.custom IS DISTINCT FROM NEW.custom THEN --if custom changed to false - apply proprties to all domain BA

        UPDATE domain_business_account ba
        SET permission_schema_id = NEW.permission_schema_id,
            twinflow_schema_id = NEW.twinflow_schema_id,
            twin_class_schema_id = NEW.twin_class_schema_id
        WHERE tier_id = NEW.id and NEW.domain_id = domain_id;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

--trigger for call stored function on changes in table domain_business_account_tier
CREATE TRIGGER tiers_tier_update_trigger
    AFTER UPDATE ON tier
    FOR EACH ROW
EXECUTE FUNCTION tiers_update_business_account_properties_on_tier_change();


-- stored function for update domain_business_account properties on its domain_tier_id change
CREATE OR REPLACE FUNCTION tiers_update_business_account_properties_on_self_tier_id_change()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE domain_business_account
    SET
        permission_schema_id = t.permission_schema_id,
        twinflow_schema_id = t.twinflow_schema_id,
        twin_class_schema_id = t.twin_class_schema_id
    FROM (SELECT permission_schema_id, twinflow_schema_id, twin_class_schema_id FROM tier WHERE id = NEW.tier_id) AS t
    WHERE domain_business_account.id = NEW.id;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

--trigger for call stored function on change domain_tier_id in domain_business_account
CREATE TRIGGER tiers_domain_business_account_tier_id_update_trigger
    AFTER UPDATE OF tier_id ON domain_business_account
    FOR EACH ROW
    WHEN (OLD.tier_id IS DISTINCT FROM NEW.tier_id)
EXECUTE FUNCTION tiers_update_business_account_properties_on_self_tier_id_change();
