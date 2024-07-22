INSERT INTO public.i18n_type (id, name) VALUES ('twinflowName'::varchar, 'Twinflow name'::varchar(255)) on conflict (id) do nothing;
INSERT INTO public.i18n_type (id, name) VALUES ('twinflowDescription'::varchar, 'Twinflow description'::varchar(255)) on conflict (id) do nothing;

alter table public.twinflow add if not exists name_i18n_id uuid;
alter table public.twinflow add if not exists description_i18n_id uuid;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name='twinflow'
        AND column_name='name'
    ) THEN
        CREATE TEMP TABLE temp_twinflow_updates AS
        SELECT id, gen_random_uuid() AS name_i18n_id, gen_random_uuid() AS description_i18n_id, name
        FROM public.twinflow WHERE name IS NOT NULL;

        INSERT INTO i18n (id, name, i18n_type_id, key)
        SELECT name_i18n_id, 'twinflow_name', 'twinflowName', null
        FROM temp_twinflow_updates;

        INSERT INTO i18n (id, name, i18n_type_id, key)
        SELECT description_i18n_id, 'twinflow_description', 'twinflowDescription', null
        FROM temp_twinflow_updates;

        INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
        SELECT name_i18n_id, 'en', name, 0
        FROM temp_twinflow_updates;

        INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
        SELECT description_i18n_id, 'en', name, 0
        FROM temp_twinflow_updates;

        UPDATE public.twinflow tf SET name_i18n_id = tmp.name_i18n_id, description_i18n_id = tmp.description_i18n_id
        FROM temp_twinflow_updates tmp WHERE tf.id = tmp.id;

        DROP TABLE temp_twinflow_updates;
    END IF;
END $$;

DROP VIEW public.twinflow_transition_lazy;
CREATE OR REPLACE VIEW public.twinflow_transition_lazy AS
SELECT tft.id,
       tft.twinflow_id,
       tc.key AS fk_twinflow_twinclass_key,
       tft.name_i18n_id,
       tft.src_twin_status_id,
       tft.dst_twin_status_id,
       tft.screen_id,
       tft.permission_id,
       psn.key,
       tft.created_at,
       tft.created_by_user_id,
       tft.allow_comment,
       tft.allow_attachments,
       tft.allow_links,
       tft.inbuilt_twin_factory_id,
       tft.drafting_twin_factory_id,
       tfta.alias AS twinflow_transition_alias,
       tc2.key AS fk_src_status_twinclass_key,
       tc3.key AS fk_dst_status_twinclass_key,
       ts1.key AS fk_src_status_name,
       ts2.key AS fk_dst_status_name
FROM twinflow_transition tft
         LEFT JOIN twinflow tf ON tft.twinflow_id = tf.id
         LEFT JOIN twin_class tc ON tf.twin_class_id = tc.id
         LEFT JOIN permission psn ON tft.permission_id = psn.id
         LEFT JOIN twin_status ts2 ON tft.dst_twin_status_id = ts2.id
         LEFT JOIN twin_class tc3 ON ts2.twins_class_id = tc3.id
         LEFT JOIN twin_status ts1 ON tft.src_twin_status_id = ts1.id
         LEFT JOIN twin_class tc2 ON ts1.twins_class_id = tc2.id
         LEFT JOIN twinflow_transition_alias tfta ON tft.twinflow_transition_alias_id = tfta.id;


alter table public.twinflow drop column if exists description;
alter table public.twinflow drop column if exists name;
