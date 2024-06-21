-- add new column
alter table twinflow_transition_alias
    add if not exists alias varchar;
-- transfer data from column ID in Alias, and generating uuid
update twinflow_transition_alias set alias = id, id = gen_random_uuid() where twinflow_transition_alias.alias is null;
-- set column not null
alter table twinflow_transition_alias
    alter column alias set not null;
-- drop constraint
alter table twinflow_transition
    drop constraint if exists twinflow_transition_twinflow_transition_alias_id_fk;
-- drop view (because need set new type to twinflow_transition)
drop view if exists  twinflow_transition_lazy;
-- set column new type
alter table twinflow_transition
    alter column twinflow_transition_alias_id type uuid using twinflow_transition_alias_id::uuid;
-- set column new type
alter table twinflow_transition_alias
    alter column id type uuid using id::uuid;
-- create view
CREATE VIEW twinflow_transition_lazy
            (id, twinflow_id, fk_twinflow_name, fk_twinflow_twinclass_key, name_i18n_id, src_twin_status_id,
             dst_twin_status_id, screen_id, permission_id, key, created_at, created_by_user_id, allow_comment,
             allow_attachments, allow_links, inbuilt_twin_factory_id, drafting_twin_factory_id,
             twinflow_transition_alias, fk_src_status_twinclass_key, fk_dst_status_twinclass_key, fk_src_status_name,
             fk_dst_status_name)
AS
SELECT tft.id,
       tft.twinflow_id,
       tf.name AS fk_twinflow_name,
       tc.key  AS fk_twinflow_twinclass_key,
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
       tfta.alias,
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
-- add new fk
alter table twinflow_transition
    add constraint twinflow_transition_twinflow_transition_alias_id_fk
        foreign key (twinflow_transition_alias_id) references twinflow_transition_alias on update cascade on delete cascade;
-- add new index
create unique index if not exists twinflow_transition_alias_domain_id_alias_uindex
    on twinflow_transition_alias (domain_id, alias);
