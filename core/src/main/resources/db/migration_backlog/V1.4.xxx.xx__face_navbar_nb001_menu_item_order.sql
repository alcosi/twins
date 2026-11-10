ALTER TABLE face_navbar_nb001_menu_item
    ADD COLUMN IF NOT EXISTS "order" integer NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_face_navbar_nb001_menu_item_sibling_order
    ON face_navbar_nb001_menu_item (face_id, parent_face_navbar_nb001_menu_item_id, "order");

ALTER TABLE face_navbar_nb001_menu_item
    ALTER COLUMN target_page_face_id DROP NOT NULL;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (2729, 27, '', '', '', false)
    ON CONFLICT DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (2730, 27, '', '', '', false)
    ON CONFLICT DO NOTHING;

INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (2731, 27, '', '', '', false)
    ON CONFLICT DO NOTHING;

-- TWINS-943: face component WT008 "Twins comments widget" — comments of the requested twin.
-- Config table face_wt008 mirrors face_wt003 (V1.3.297.02) / face_tw007 (V1.3.370.02) layout.
-- label_i18n_id is nullable on purpose: empty label falls back to FE default "Comments"
-- (I18nCacheHolder.addId(null) returns "").

insert into face_component (id, face_component_type_id, name, description)
values ('WT008', 'WIDGET', 'Twins comments widget', 'Comments of the requested twin')
    on conflict do nothing;

create table if not exists face_wt008
(
    id                            uuid    not null
    primary key,
    face_id                       uuid    not null
    references face
    on update cascade on delete cascade,
    twin_pointer_validator_rule_id uuid
    references twin_pointer_validator_rule
    on update cascade on delete cascade,
    key                           varchar,
    label_i18n_id                 uuid
    references i18n
    on update cascade on delete restrict,
    show_chart                    boolean not null default true,
    style_classes                 varchar
);

create index if not exists face_wt008_face_id_idx
    on face_wt008 (face_id);

create index if not exists face_wt008_twin_pointer_validator_rule_id_idx
    on face_wt008 (twin_pointer_validator_rule_id);

create index if not exists face_wt008_label_i18n_id_idx
    on face_wt008 (label_i18n_id);


