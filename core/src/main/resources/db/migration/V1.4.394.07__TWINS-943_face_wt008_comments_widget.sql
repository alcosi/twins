-- TWINS-943: face component WT008 "Twins comments widget" — comments of the requested twin.
-- Config table face_wt008 mirrors face_wt003 (V1.3.297.02) / face_tw007 (V1.3.370.02) layout,
-- extended with target_twin_pointer_id (WT008 is a pointed widget from birth, see
-- V1.4.394.06__TWINS-943_face_widget_target_twin_pointer.sql).
-- Comments are fetched via the configured comment search (TWINS-946): the widget config
-- returns twin_comment_search_id and the client calls /private/comment/search/{searchId}/v1
-- with narrow.twinIdList = [pointedTwinId]. Defaults to the UNLIMITED system row
-- (SystemIds.TwinCommentSearch.UNLIMITED) = "no configured predicates" (legacy behavior).
-- DEPENDENCY: twin_comment_search is created by TWINS-946 (V1.4.xx.02) — it must run before this migration.
-- label_i18n_id is nullable on purpose: empty label falls back to FE default "Comments"
-- (I18nCacheHolder.addId(null) returns "").

insert into face_component (id, face_component_type_id, name, description)
values ('WT008', 'WIDGET', 'Twins comments widget', 'Comments of the requested twin')
    on conflict do nothing;

create table if not exists face_wt008
(
    id                             uuid    not null
        primary key,
    face_id                        uuid    not null
        references face
        on update cascade on delete cascade,
    twin_pointer_validator_rule_id uuid
        references twin_pointer_validator_rule
        on update cascade on delete cascade,
    target_twin_pointer_id         uuid
        references twin_pointer
        on update cascade on delete cascade,
    key                            varchar,
    label_i18n_id                  uuid
        references i18n
        on update cascade on delete restrict,
    twin_comment_search_id         uuid    not null default '00000000-0000-0000-0014-000000000006'
        references twin_comment_search
        on update cascade on delete restrict,
    style_classes                  varchar
);

create index if not exists face_wt008_face_id_idx
    on face_wt008 (face_id);

create index if not exists face_wt008_twin_pointer_validator_rule_id_idx
    on face_wt008 (twin_pointer_validator_rule_id);

create index if not exists face_wt008_target_twin_pointer_id_idx
    on face_wt008 (target_twin_pointer_id);

create index if not exists face_wt008_label_i18n_id_idx
    on face_wt008 (label_i18n_id);

create index if not exists face_wt008_twin_comment_search_id_idx
    on face_wt008 (twin_comment_search_id);
