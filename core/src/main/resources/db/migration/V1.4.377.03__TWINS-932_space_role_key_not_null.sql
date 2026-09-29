-- TWINS-932: make space_role.key mandatory
-- Backfills existing NULL/empty keys with a deterministic value derived from id ('role_<id>'),
-- then enforces NOT NULL (unique index space_role_twin_class_id_key_uindex already exists).

UPDATE public.space_role SET key = 'role_' || id WHERE key IS NULL OR length(btrim(key)) = 0;

ALTER TABLE public.space_role ALTER COLUMN key SET NOT NULL;
