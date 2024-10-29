CREATE TABLE  if not exists public.domain_business_account_tier (
            id uuid NOT NULL,
            domain_id uuid,
            name character varying,
            custom boolean DEFAULT false,
            permission_schema_id uuid,
            twinflow_schema_id uuid,
            twin_class_schema_id uuid,
            attachments_storage_quota_count integer NOT NULL default 0,
            attachments_storage_quota_size integer NOT NULL default 0,
            user_count_quota integer NOT NULL default 0,
            description character varying
);
alter table public.domain drop constraint if exists domain_to_domain_business_account_tier_id_fk;
alter table public.domain_business_account drop constraint if exists domain_business_account_domain_business_account_tier_id_fk;
alter table public.domain_business_account_tier drop constraint if exists domain_business_account_tier_pk;
alter table public.domain_business_account_tier drop constraint if exists domain_business_account_tier_domain_id_fk;
alter table public.domain_business_account_tier drop constraint if exists domain_business_account_tier_permission_schema_id_fk;
alter table public.domain_business_account_tier drop constraint if exists domain_business_account_tier_twinflow_schema_id_fk;
alter table public.domain_business_account_tier drop constraint if exists domain_business_account_tier_twin_class_schema_id_fk;

ALTER TABLE ONLY public.domain_business_account_tier ADD CONSTRAINT domain_business_account_tier_pk PRIMARY KEY (id);
ALTER TABLE ONLY public.domain_business_account_tier ADD CONSTRAINT domain_business_account_tier_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id);
ALTER TABLE ONLY public.domain_business_account_tier ADD CONSTRAINT domain_business_account_tier_permission_schema_id_fk FOREIGN KEY (permission_schema_id) REFERENCES public.permission_schema(id);
ALTER TABLE ONLY public.domain_business_account_tier ADD CONSTRAINT domain_business_account_tier_twinflow_schema_id_fk FOREIGN KEY (twinflow_schema_id) REFERENCES public.twinflow_schema(id);
ALTER TABLE ONLY public.domain_business_account_tier ADD CONSTRAINT domain_business_account_tier_twin_class_schema_id_fk FOREIGN KEY (twin_class_schema_id) REFERENCES public.twin_class_schema(id);

alter table public.domain add column if not exists default_domain_business_account_tier_id uuid;
ALTER TABLE ONLY public.domain ADD CONSTRAINT domain_to_domain_business_account_tier_id_fk FOREIGN KEY (default_domain_business_account_tier_id) REFERENCES public.domain_business_account_tier(id);

alter table public.domain_business_account add column if not exists domain_tier_id uuid;
ALTER TABLE ONLY public.domain_business_account ADD CONSTRAINT domain_business_account_domain_business_account_tier_id_fk FOREIGN KEY (domain_tier_id) REFERENCES public.domain_business_account_tier(id);



DROP TRIGGER IF EXISTS tiers_domain_business_account_tier_update_trigger ON public.domain_business_account_tier;
DROP TRIGGER IF EXISTS tiers_domain_business_account_domain_tier_id_update_trigger ON public.domain_business_account;
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
        WHERE domain_tier_id = NEW.id and NEW.domain_id = domain_id;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

--trigger for call stored function on changes in table domain_business_account_tier
CREATE TRIGGER tiers_domain_business_account_tier_update_trigger
    AFTER UPDATE ON domain_business_account_tier
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
    FROM (SELECT permission_schema_id, twinflow_schema_id, twin_class_schema_id FROM domain_business_account_tier WHERE id = NEW.domain_tier_id) AS t
    WHERE domain_business_account.id = NEW.id;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

--trigger for call stored function on change domain_tier_id in domain_business_account
CREATE TRIGGER tiers_domain_business_account_domain_tier_id_update_trigger
    AFTER UPDATE OF domain_tier_id ON domain_business_account
    FOR EACH ROW
    WHEN (OLD.domain_tier_id IS DISTINCT FROM NEW.domain_tier_id)
EXECUTE FUNCTION tiers_update_business_account_properties_on_self_tier_id_change();

INSERT INTO public.domain_business_account_tier (id, domain_id, name, custom, permission_schema_id, twinflow_schema_id, twin_class_schema_id, attachments_storage_quota_count, attachments_storage_quota_size, user_count_quota, description) VALUES ('64807201-e3d6-4016-b699-b36c5f91c58e'::uuid, 'f67ad556-dd27-4871-9a00-16fb0e8a4102'::uuid, 'Gold'::varchar, false::boolean, 'af143656-9899-4e1f-8683-48795cdefeac'::uuid, '2c618b09-e8dc-4712-a433-2e18915ee70d'::uuid, '8b9ea6ad-2b9b-4a4a-8ea9-1b17da4d603b'::uuid, 0::integer, 0::integer, 0::integer, 'Gold'::varchar) on conflict (id) do nothing;
INSERT INTO public.domain_business_account_tier (id, domain_id, name, custom, permission_schema_id, twinflow_schema_id, twin_class_schema_id, attachments_storage_quota_count, attachments_storage_quota_size, user_count_quota, description) VALUES ('64807201-e3d6-4016-b699-b36c5f91c60e'::uuid, 'f67ad556-dd27-4871-9a00-16fb0e8a4102'::uuid, 'Free'::varchar, false::boolean, 'af143656-9899-4e1f-8683-48795cdefeac'::uuid, '2c618b09-e8dc-4712-a433-2e18915ee70d'::uuid, '8b9ea6ad-2b9b-4a4a-8ea9-1b17da4d603b'::uuid, 0::integer, 0::integer, 0::integer, 'Free'::varchar) on conflict (id) do nothing;
INSERT INTO public.domain_business_account_tier (id, domain_id, name, custom, permission_schema_id, twinflow_schema_id, twin_class_schema_id, attachments_storage_quota_count, attachments_storage_quota_size, user_count_quota, description) VALUES ('64807201-e3d6-4016-b699-b36c5f91c57e'::uuid, 'f67ad556-dd27-4871-9a00-16fb0e8a4102'::uuid, 'Platium'::varchar, false::boolean, 'af143656-9899-4e1f-8683-48795cdefeac'::uuid, '2c618b09-e8dc-4712-a433-2e18915ee70d'::uuid, '8b9ea6ad-2b9b-4a4a-8ea9-1b17da4d603b'::uuid, 0::integer, 0::integer, 0::integer, 'Platium'::varchar) on conflict (id) do nothing;
INSERT INTO public.domain_business_account_tier (id, domain_id, name, custom, permission_schema_id, twinflow_schema_id, twin_class_schema_id, attachments_storage_quota_count, attachments_storage_quota_size, user_count_quota, description) VALUES ('64807201-e3d6-4016-b699-b36c5f91c61e'::uuid, 'f67ad556-dd27-4871-9a00-16fb0e8a4102'::uuid, 'Corporation'::varchar, false::boolean, 'af143656-9899-4e1f-8683-48795cdefeac'::uuid, '2c618b09-e8dc-4712-a433-2e18915ee70d'::uuid, '8b9ea6ad-2b9b-4a4a-8ea9-1b17da4d603b'::uuid, 0::integer, 0::integer, 0::integer, 'Corporation'::varchar) on conflict (id) do nothing;
INSERT INTO public.domain_business_account_tier (id, domain_id, name, custom, permission_schema_id, twinflow_schema_id, twin_class_schema_id, attachments_storage_quota_count, attachments_storage_quota_size, user_count_quota, description) VALUES ('64807201-e3d6-4016-b699-b36c5f91c59e'::uuid, 'f67ad556-dd27-4871-9a00-16fb0e8a4102'::uuid, 'Silver'::varchar, false::boolean, 'af143656-9899-4e1f-8683-48795cdefeac'::uuid, '2c618b09-e8dc-4712-a433-2e18915ee70d'::uuid, '8b9ea6ad-2b9b-4a4a-8ea9-1b17da4d603b'::uuid, 0::integer, 0::integer, 0::integer, 'Silver'::varchar) on conflict (id) do nothing;
INSERT INTO public.domain_business_account_tier (id, domain_id, name, custom, permission_schema_id, twinflow_schema_id, twin_class_schema_id, attachments_storage_quota_count, attachments_storage_quota_size, user_count_quota, description) VALUES ('64807201-e3d6-4016-b699-b36c5f91c56e'::uuid, 'f67ad556-dd27-4871-9a00-16fb0e8a4102'::uuid, 'Titanium'::varchar, false::boolean, 'af143656-9899-4e1f-8683-48795cdefeac'::uuid, '2c618b09-e8dc-4712-a433-2e18915ee70d'::uuid, '8b9ea6ad-2b9b-4a4a-8ea9-1b17da4d603b'::uuid, 0::integer, 0::integer, 0::integer, 'Titanium'::varchar) on conflict (id) do nothing;
INSERT INTO public.domain_business_account_tier (id, domain_id, name, custom, permission_schema_id, twinflow_schema_id, twin_class_schema_id, attachments_storage_quota_count, attachments_storage_quota_size, user_count_quota, description) VALUES ('74807201-e3d6-4016-b699-b36c5f91c60e'::uuid, 'f01a8f45-6fad-4189-a320-bab0124d8bcb'::uuid, 'Free'::varchar, false::boolean, null::uuid, null::uuid, null::uuid, 0::integer, 0::integer, 0::integer, 'Free'::varchar) on conflict (id) do nothing;

-- set silver to all BA
UPDATE public.domain_business_account SET domain_tier_id = '64807201-e3d6-4016-b699-b36c5f91c59e'::uuid;
-- set free(for alcosi domain) to single alcosi BA
UPDATE public.domain_business_account SET domain_tier_id = '74807201-e3d6-4016-b699-b36c5f91c60e'::uuid WHERE id = '64184024-7337-41b3-8f03-50ff75cd270f'::uuid;

-- set "free" tiers as defaukt tiers for domains
UPDATE public.domain SET default_domain_business_account_tier_id = '64807201-e3d6-4016-b699-b36c5f91c60e'::uuid WHERE id = 'f01a8f45-6fad-4189-a320-bab0124d8bcb'::uuid;                                                                                                                    [2024-10-29 15:21:09] 1 row affected in 3 ms
UPDATE public.domain SET default_domain_business_account_tier_id = '74807201-e3d6-4016-b699-b36c5f91c60e'::uuid WHERE id = 'f67ad556-dd27-4871-9a00-16fb0e8a4102'::uuid;

alter table public.domain_business_account alter column domain_tier_id set not null;
