-- TWINS-952: face component WT011 "Twin form widget" — declarative twin form with a fully
-- configurable layout: Form -> Columns -> Sections -> Rows -> Fields.
-- The main row is a pointed variant (FacePointedEntity): twin_pointer_validator_rule_id picks
-- the layout variant for the current twin, target_twin_pointer_id points at the twin whose
-- data the form shows/edits (null = current twin). Every child level also carries a nullable
-- twin_pointer_validator_rule_id (FaceVariantEntity) so individual columns/sections/rows/fields
-- can be shown conditionally.
-- Widths are relative weights (flex-grow semantics): element width = weight / sum of sibling
-- weights. Equal widths = default weight 1 for all siblings.
-- Adaptive behavior (SMIC): stacking mechanics and breakpoints are hardcoded on the FE; the
-- config only overrides the defaults — face_wt011_column.adaptive_order (stacking order of
-- columns, null = same as "order") and face_wt011_row.allow_wrap (fields may wrap to a new
-- visual line on narrow screens).
-- Editability is NOT configured here: it is derived per field from the pointed twin's field
-- descriptors; the switch to full edit mode is derived from the twin's actions containing EDIT.
-- Submit/cancel buttons are rendered by the FE only in edit mode; their labels are configurable
-- (null = FE default).
-- FK actions follow existing conventions: validator rule = cascade on the main row (WT008),
-- restrict on child rows (PG002 widget); i18n = restrict; hierarchy levels = cascade.

insert into face_component (id, face_component_type_id, name, description)
values ('WT011', 'WIDGET', 'Twin form widget', 'Declarative twin form: columns, sections, rows, fields')
    on conflict do nothing;

create table if not exists face_wt011
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
    submit_label_i18n_id           uuid
        references i18n
        on update cascade on delete restrict,
    cancel_label_i18n_id           uuid
        references i18n
        on update cascade on delete restrict,
    style_classes                  varchar
);

create index if not exists face_wt011_face_id_idx
    on face_wt011 (face_id);

create index if not exists face_wt011_twin_pointer_validator_rule_id_idx
    on face_wt011 (twin_pointer_validator_rule_id);

create index if not exists face_wt011_target_twin_pointer_id_idx
    on face_wt011 (target_twin_pointer_id);

create index if not exists face_wt011_label_i18n_id_idx
    on face_wt011 (label_i18n_id);

create index if not exists face_wt011_submit_label_i18n_id_idx
    on face_wt011 (submit_label_i18n_id);

create index if not exists face_wt011_cancel_label_i18n_id_idx
    on face_wt011 (cancel_label_i18n_id);

create table if not exists face_wt011_column
(
    id                             uuid    not null
        primary key,
    face_wt011_id                  uuid    not null
        references face_wt011
        on update cascade on delete cascade,
    twin_pointer_validator_rule_id uuid
        references twin_pointer_validator_rule
        on update cascade on delete restrict,
    "order"                        integer not null default 0,
    width_weight                   integer not null default 1,
    adaptive_order                 integer,
    style_classes                  varchar
);

create index if not exists face_wt011_column_sibling_order_idx
    on face_wt011_column (face_wt011_id, "order");

create index if not exists face_wt011_column_twin_pointer_validator_rule_id_idx
    on face_wt011_column (twin_pointer_validator_rule_id);

create table if not exists face_wt011_section
(
    id                             uuid    not null
        primary key,
    face_wt011_column_id           uuid    not null
        references face_wt011_column
        on update cascade on delete cascade,
    twin_pointer_validator_rule_id uuid
        references twin_pointer_validator_rule
        on update cascade on delete restrict,
    label_i18n_id                  uuid
        references i18n
        on update cascade on delete restrict,
    "order"                        integer not null default 0,
    style_classes                  varchar
);

create index if not exists face_wt011_section_sibling_order_idx
    on face_wt011_section (face_wt011_column_id, "order");

create index if not exists face_wt011_section_twin_pointer_validator_rule_id_idx
    on face_wt011_section (twin_pointer_validator_rule_id);

create index if not exists face_wt011_section_label_i18n_id_idx
    on face_wt011_section (label_i18n_id);

create table if not exists face_wt011_row
(
    id                             uuid    not null
        primary key,
    face_wt011_section_id          uuid    not null
        references face_wt011_section
        on update cascade on delete cascade,
    twin_pointer_validator_rule_id uuid
        references twin_pointer_validator_rule
        on update cascade on delete restrict,
    "order"                        integer not null default 0,
    allow_wrap                     boolean not null default true,
    style_classes                  varchar
);

create index if not exists face_wt011_row_sibling_order_idx
    on face_wt011_row (face_wt011_section_id, "order");

create index if not exists face_wt011_row_twin_pointer_validator_rule_id_idx
    on face_wt011_row (twin_pointer_validator_rule_id);

create table if not exists face_wt011_field
(
    id                             uuid    not null primary key,
    face_wt011_row_id              uuid    not null
        references face_wt011_row
        on update cascade on delete cascade,
    twin_pointer_validator_rule_id uuid
        references twin_pointer_validator_rule
        on update cascade on delete restrict,
    twin_class_field_id            uuid    not null
        references twin_class_field
        on update cascade on delete cascade,
    label_i18n_id                  uuid
        references i18n
        on update cascade on delete restrict,
    "order"                        integer not null default 0,
    width_weight                   integer not null default 1,
    style_classes                  varchar
);

create index if not exists face_wt011_field_sibling_order_idx
    on face_wt011_field (face_wt011_row_id, "order");

create index if not exists face_wt011_field_twin_pointer_validator_rule_id_idx
    on face_wt011_field (twin_pointer_validator_rule_id);

create index if not exists face_wt011_field_twin_class_field_id_idx
    on face_wt011_field (twin_class_field_id);

create index if not exists face_wt011_field_label_i18n_id_idx
    on face_wt011_field (label_i18n_id);
