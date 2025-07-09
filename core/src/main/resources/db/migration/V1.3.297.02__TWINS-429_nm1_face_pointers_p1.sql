create table if not exists twin_pointer
(
    id                  uuid primary key,
    twin_class_id             uuid    -- nullable true if we want to have shared points
        constraint twin_pointer_twin_class_id_fk
            references twin_class
            on update cascade on delete cascade,
    pointer_featurer_id integer not null
        constraint twin_pointer_pointer_featurer_id_fk
            references featurer
            on update cascade on delete restrict,
    pointer_params      hstore,
    name                varchar
);

create index if not exists twin_pointer_twin_class_id_idx
    on twin_pointer (twin_class_id);

create index if not exists twin_pointer_pointer_featurer_id_idx
    on twin_pointer (pointer_featurer_id);


INSERT INTO twin_pointer (id, twin_class_id, pointer_featurer_id, pointer_params, name) VALUES ('00000000-0000-0000-0012-000000000001', null, 3101, null, 'Current twin pointer')
on conflict do nothing;

insert into featurer(id, featurer_type_id, class, name, description)
values (1610, 16, '', '', '')
on conflict (id) do nothing;

insert into featurer(id, featurer_type_id, class, name, description)
values (1611, 16, '', '', '')
on conflict (id) do nothing;

create table if not exists twin_pointer_validator_rule
(
    id                    uuid primary key,
    twin_pointer_id     uuid not null
        constraint twin_pointer_validator_rule_twin_pointer_id_fk
            references twin_pointer
            on update cascade on delete cascade,
    twin_validator_set_id uuid
        constraint twin_pointer_validator_rule_twin_validator_set_id_fk
            references twin_validator_set
            on update cascade on delete cascade
);


CREATE TABLE if not exists face_pg001
(
    id                                  UUID PRIMARY KEY,
    face_id                             UUID NOT NULL
        references face
            on update cascade on delete cascade,
    twin_pointer_validator_rule_id UUID
        references twin_pointer_validator_rule
            on update cascade on delete cascade,
    title_i18n_id                       UUID
        references i18n
            ON UPDATE CASCADE,
    style_classes                       VARCHAR(255)
);

create index if not exists face_pg001_face_id_idx
    on face_pg001 (face_id);

create index if not exists face_pg001_twin_pointer_validator_rule_id_idx
    on face_pg001 (twin_pointer_validator_rule_id);

create index if not exists face_pg001_title_i18n_id_idx
    on face_pg001 (title_i18n_id);


create table if not exists face_pg001_widget
(
    id                                  uuid                 not null
            primary key,
    face_pg001_id                       uuid                 not null
        constraint face_pg001_widget_face_pg001_id_fk
            references face_pg001
            on update cascade on delete restrict,
    twin_pointer_validator_rule_id UUID
        constraint face_pg001_widget_twin_pointer_validator_rule_id_fk
            references twin_pointer_validator_rule
            on update cascade on delete cascade,
    widget_face_id                      uuid                 not null
        constraint face_pg001_widget_widget_face_id_fk
            references face
            on update cascade on delete cascade,
    active                              boolean default true not null,
    style_classes                       varchar(255)
);

create index if not exists face_pg001_widget_face_pg001_id_idx
    on face_pg001_widget (face_pg001_id);

create index if not exists face_pg001_widget_twin_pointer_validator_rule_id_idx
    on face_pg001_widget (twin_pointer_validator_rule_id);

create index if not exists face_pg001_widget_widget_face_id_idx
    on face_pg001_widget (widget_face_id);

create table if not exists face_pg002_layout
(
    id          varchar(40) not null
        primary key,
    description varchar(255)
);


create table if not exists face_pg002
(

    id                                  UUID PRIMARY KEY,
    face_id                             UUID        NOT NULL
        references face
            on update cascade on delete cascade,
    twin_pointer_validator_rule_id UUID
        references twin_pointer_validator_rule
            on update cascade on delete restrict,
    title_i18n_id                       uuid
        references i18n
            on update cascade on delete restrict,
    face_pg002_layout_id                varchar(40) not null
        constraint face_pg002_face_pg002_layout_id_fk
            references face_pg002_layout
            on update cascade on delete restrict,
    style_classes                       varchar(255)
);

create index if not exists face_pg002_face_id_idx
    on face_pg002 (face_id);

create index if not exists face_pg002_twin_pointer_validator_rule_id_idx
    on face_pg002 (twin_pointer_validator_rule_id);

create index if not exists face_pg002_title_i18n_id_idx
    on face_pg002 (title_i18n_id);

create index if not exists face_pg002_face_pg002_layout_id_idx
    on face_pg002 (face_pg002_layout_id);

create table if not exists face_pg002_tab
(
    id                                  uuid                  not null
        primary key,
    face_pg002_id                       uuid                  not null
        references face_pg002
            on update cascade on delete cascade,
    twin_pointer_validator_rule_id UUID
        references twin_pointer_validator_rule
            on update cascade on delete restrict,
    icon_resource_id                    uuid
        references resource
            on update cascade on delete restrict,
    title_i18n_id                       uuid
        references i18n
            on update cascade on delete restrict,
    active                              boolean  default true not null,
    style_classes                       varchar(255),
    "order"                             smallint default 0    not null
);

create index if not exists face_pg002_tab_face_pg002_id_idx
    on face_pg002_tab (face_pg002_id);

create index if not exists face_pg002_tab_twin_pointer_validator_rule_id_idx
    on face_pg002_tab (twin_pointer_validator_rule_id);

create index if not exists face_pg002_tab_icon_resource_id_idx
    on face_pg002_tab (icon_resource_id);

create index if not exists face_pg002_tab_title_i18n_id_idx
    on face_pg002_tab (title_i18n_id);



create table if not exists face_pg002_widget
(
    id                                  uuid                 not null
        primary key,
    face_pg002_tab_id                   uuid                 not null
        constraint face_pg002_widget_face_pg002_tab_id_fk
            references face_pg002_tab
            on update cascade on delete cascade,
    twin_pointer_validator_rule_id UUID
        constraint face_pg002_widget_twin_pointer_validator_rule_id_fk
            references twin_pointer_validator_rule
            on update cascade on delete restrict ,
    widget_face_id                      uuid                 not null
        constraint face_pg002_widget_widget_face_id_fk
            references face
            on update cascade on delete cascade,
    active                              boolean default true not null,
    style_classes                       varchar(255)
);

create index if not exists face_pg002_widget_face_pg002_tab_id_idx
    on face_pg002_widget (face_pg002_tab_id);

create index if not exists face_pg002_widget_twin_pointer_validator_rule_id_idx
    on face_pg002_widget (twin_pointer_validator_rule_id);

create index if not exists face_pg002_widget_widget_face_id_idx
    on face_pg002_widget (widget_face_id);


create table if not exists face_tw001
(
    id                                  uuid    not null
        primary key,
    face_id                             uuid    not null
        references face
            on update cascade on delete cascade ,
    twin_pointer_validator_rule_id UUID
        references twin_pointer_validator_rule
            on update cascade on delete cascade,
    target_twin_pointer_id     uuid,
    key                                 varchar not null,
    label_i18n_id                       uuid
        constraint face_tw001_label_i18n_id_fk
            references i18n
            on update cascade on delete restrict ,
    images_twin_class_field_id          uuid
        constraint face_tw001_images_twin_class_field_id_fk
            references twin_class_field
            on update cascade on delete cascade
);

create index if not exists face_tw001_face_id_idx
    on face_tw001 (face_id);

create index if not exists face_tw001_twin_pointer_validator_rule_id_idx
    on face_tw001 (twin_pointer_validator_rule_id);

create index if not exists face_tw001_label_i18n_id_idx
    on face_tw001 (label_i18n_id);

create index if not exists face_tw001_images_twin_class_field_id_idx
    on face_tw001 (images_twin_class_field_id);


create table if not exists face_tw002
(
    id                                  uuid    not null
        primary key,
    face_id                             uuid    not null
        references face
            on update cascade on delete restrict,
    twin_pointer_validator_rule_id UUID
        references twin_pointer_validator_rule
            on update cascade on delete cascade,
    target_twin_pointer_id     uuid,
    key                                 varchar not null,
    label_i18n_id                       uuid
        constraint face_tw002_label_i18n_id_fk
            references i18n
            on update cascade,
    i18n_twin_class_field_id            uuid    not null
        constraint face_tw002_i18n_twin_class_field_id_fk
            references twin_class_field
            on update cascade
);

create index if not exists face_tw002_face_id_ids
    on face_tw002 (face_id);

create index if not exists face_tw002_twin_pointer_validator_rule_id_ids
    on face_tw002 (twin_pointer_validator_rule_id);

create index if not exists face_tw002_label_i18n_id_ids
    on face_tw002 (label_i18n_id);

create index if not exists face_tw002_i18n_twin_class_field_id_ids
    on face_tw002 (i18n_twin_class_field_id);


create table if not exists face_tw002_accordion_item
(
    id            uuid       not null
        primary key,
    face_tw002_id uuid       not null
        references face_tw002
            on update cascade on delete cascade,
    locale        varchar(2) not null
        constraint face_tw002_accordion_item_locale_id_fk
            references i18n_locale
            on update cascade,
    label_i18n_id uuid       not null
        constraint face_tw002_accordion_item_label_i18n_id_fk
            references i18n
            on update cascade
);

create index if not exists face_tw002_accordion_item_idx
    on face_tw002_accordion_item (face_tw002_id);

create index if not exists face_tw002_accordion_item_locale_idx
    on face_tw002_accordion_item (locale);

create index if not exists face_tw002_accordion_item_label_i18n_id_idx
    on face_tw002_accordion_item (label_i18n_id);

create table if not exists face_tw004
(
    id                                  uuid    not null
        primary key,
    face_id                             uuid    not null
        references face
            on update cascade on delete restrict,
    twin_pointer_validator_rule_id UUID
        references twin_pointer_validator_rule
            on update cascade on delete cascade,
    target_twin_pointer_id     uuid,
    key                                 varchar not null,
    field_finder_featurer_id            integer not null
        constraint face_tw004_field_finder_featurer_id_fk
            references featurer,
    field_finder_params                 hstore,
    editable_field_filter_featurer_id   integer
        references featurer
            on update cascade on delete cascade,
    editable_field_filter_params        hstore
);

create index if not exists face_tw004_face_id_idx
    on face_tw004 (face_id);

create index if not exists face_tw004_twin_pointer_validator_rule_id_idx
    on face_tw004 (twin_pointer_validator_rule_id);

create index if not exists face_tw004_field_finder_featurer_id_idx
    on face_tw004 (field_finder_featurer_id);

create index if not exists face_tw004_editable_field_filter_featurer_id_idx
    on face_tw004 (editable_field_filter_featurer_id);


create table if not exists face_tw005
(
    id                                  uuid                  not null
        primary key,
    face_id                             uuid                  not null
        references face
            on update cascade on delete cascade,
    twin_pointer_validator_rule_id UUID
        references twin_pointer_validator_rule
            on update cascade on delete cascade,
    target_twin_pointer_id     uuid,
    align_vertical                      boolean default false not null,
    glue                                boolean default false not null,
    style_classes                       varchar
);

create index if not exists face_tw005_face_id_idx
    on face_tw005 (face_id);

create index if not exists face_tw005_face_id_idx
    on face_tw005 (twin_pointer_validator_rule_id);


create table if not exists face_tw005_button
(
    id                                  uuid                  not null
        primary key,
    face_tw005_id                       uuid                  not null
        references face_tw005
            on update cascade on delete cascade,
    twin_pointer_validator_rule_id UUID
        references twin_pointer_validator_rule
            on update cascade on delete cascade,
    twinflow_transition_id              uuid                  not null
        constraint face_tw005_button_twinflow_transition_id_fk
            references twinflow_transition
            on update cascade on delete cascade,
    label_i18n_id                       uuid
        constraint face_tw005_button_label_i18n_id_fk
            references i18n
            on update cascade,
    icon_resource_id                    uuid
        constraint face_tw005_button_icon_resource_id_fk
            references resource
            on update cascade,
    "order"                             integer default 1     not null,
    active                              boolean default true  not null,
    style_classes                       varchar,
    show_when_inactive                  boolean default false not null
);

create index if not exists face_tw005_button_face_tw005_id_idx
    on face_tw005_button (face_tw005_id);

create index if not exists face_tw005_button_twin_pointer_validator_rule_id_idx
    on face_tw005_button (twin_pointer_validator_rule_id);

create index if not exists face_tw005_button_twinflow_transition_id_idx
    on face_tw005_button (twinflow_transition_id);

create index if not exists face_tw005_button_label_i18n_id_idx
    on face_tw005_button (label_i18n_id);

create index if not exists face_tw005_button_icon_resource_id_idx
    on face_tw005_button (icon_resource_id);

create table if not exists face_wt001
(
    id                                  uuid                 not null
        primary key,
    face_id                             uuid                 not null
        references face
            on update cascade on delete restrict,
    twin_pointer_validator_rule_id UUID
        references twin_pointer_validator_rule
            on update cascade on delete cascade,
    key                                 varchar              not null,
    label_i18n_id                       uuid                 not null
        constraint face_wt001_label_i18n_id_fk
            references i18n
            on update cascade,
    twin_class_id                       uuid                 not null
        constraint face_wt001_twin_class_id_fk
            references twin_class
            on update cascade,
    search_id                           uuid
        constraint face_wt001_search_id_fk
            references search
            on update cascade,
    show_create_button                  boolean default true not null,
    modal_face_id uuid
        references face
            on update cascade on delete cascade
);

create index if not exists face_wt001_face_id_idx
    on face_wt001 (face_id);

create index if not exists face_wt001_twin_pointer_validator_rule_id_idx
    on face_wt001 (twin_pointer_validator_rule_id);

create index if not exists face_wt001_label_i18n_id_idx
    on face_wt001 (label_i18n_id);

create index if not exists face_wt001_twin_class_id_idx
    on face_wt001 (twin_class_id);

create index if not exists face_wt001_modal_face_id_idx
    on face_wt001 (modal_face_id);

create table if not exists face_wt001_column
(
    id                                  uuid                  not null
        primary key,
    face_wt001_id                       uuid                  not null
        references face_wt001
            on update cascade on delete cascade,
    twin_pointer_validator_rule_id UUID
        references twin_pointer_validator_rule
            on update cascade on delete cascade,
    twin_class_field_id                 uuid                  not null
        references twin_class_field
            on update cascade on delete cascade,
    "order"                             integer               not null,
    label_i18n_id                       uuid
        references i18n
            on update cascade on delete restrict,
    show_by_default                     boolean default false not null
);

create index if not exists face_wt001_column_face_wt001_column_idx
    on face_wt001_column (face_wt001_id);

create index if not exists face_wt001_column_twin_pointer_validator_rule_id_id_idx
    on face_wt001_column (twin_pointer_validator_rule_id);

create index if not exists face_wt001_column_twin_class_field_id_ids
    on face_wt001_column (twin_class_field_id);

create index if not exists face_wt001_column_label_i18n_id_idx
    on face_wt001_column (label_i18n_id);

create table if not exists face_wt002
(
    id                                  uuid    not null
        primary key,
    face_id                             uuid    not null
        references face
            on update cascade on delete cascade,
    twin_pointer_validator_rule_id UUID
        references twin_pointer_validator_rule
            on update cascade on delete cascade,
    key                                 varchar not null,
    style_classes                       varchar
);

create index if not exists face_wt002_face_id_idx
    on face_wt002 (face_id);

create index if not exists face_wt002_twin_pointer_validator_rule_id_idx
    on face_wt002 (twin_pointer_validator_rule_id);


create table if not exists face_wt002_button
(
    id                                  uuid              not null
        primary key,
    face_wt002_id                       uuid              not null
        references face_wt002
            on update cascade on delete cascade,
    twin_pointer_validator_rule_id UUID
        references twin_pointer_validator_rule
            on update cascade on delete cascade,
    key                                 varchar           not null,
    label_i18n_id                       uuid
        references i18n
            on update cascade on delete restrict,
    icon_resource_id                    uuid
        references resource
            on update cascade on delete restrict,
    style_classes                       varchar,
    modal_face_id    uuid not null
            references face
                on update cascade on delete restrict
);

create index if not exists face_wt002_button_face_wt002_id_idx
    on face_wt002_button (face_wt002_id);

create index if not exists face_wt002_button_twin_pointer_validator_rule_id_idx
    on face_wt002_button (twin_pointer_validator_rule_id);

create index if not exists face_wt002_button_label_i18n_id_idx
    on face_wt002_button (label_i18n_id);

create index if not exists face_wt002_button_icon_resource_id_idx
    on face_wt002_button (icon_resource_id);

create index if not exists face_wt002_button_modal_face_id_idx
    on face_wt002_button (modal_face_id);


create table if not exists face_wt003
(
    id                                  uuid    not null
        primary key,
    face_id                             uuid    not null
        references face
            on update cascade on delete cascade,
    twin_pointer_validator_rule_id UUID
        references twin_pointer_validator_rule
            on update cascade on delete cascade,
    level                               varchar not null,
    title_i18n_id                       uuid
        references i18n
            on update cascade on delete restrict,
    message_i18n_id                     uuid
        references i18n
            on update cascade on delete restrict,
    icon_resource_id                    uuid
        references resource
            on update cascade on delete restrict,
    style_classes                       varchar
);

create index if not exists face_wt003_face_id_idx
    on face_wt003 (face_id);

create index if not exists face_wt003_twin_pointer_validator_rule_id_idx
    on face_wt003 (twin_pointer_validator_rule_id);

create index if not exists face_wt003_title_i18n_id_idx
    on face_wt003 (title_i18n_id);

create index if not exists face_wt003_message_i18n_id_idx
    on face_wt003 (message_i18n_id);

create index if not exists face_wt003_icon_resource_id_idx
    on face_wt003 (icon_resource_id);

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'face_tc001'
              AND column_name = 'id'
        ) THEN
            EXECUTE 'DROP TABLE IF EXISTS face_tc001';
        END IF;
    END
$$;

create table if not exists face_tc001
(
    id                                  uuid    not null
        primary key,
    face_id                      uuid              not null
        references face
            on update cascade on delete cascade,
    twin_pointer_validator_rule_id UUID
        references twin_pointer_validator_rule
            on update cascade on delete cascade,
    key                          varchar           not null,
    class_selector_label_i18n_id uuid
        references i18n
            on update cascade on delete restrict,
    save_button_label_i18n_id    uuid
        references i18n
            on update cascade on delete restrict,
    header_i18n_id               uuid
        references i18n
            on update cascade on delete restrict,
    header_icon_resource_id      uuid
        references resource
            on update cascade on delete restrict,
    style_classes                varchar,
    twin_class_id                uuid              not null
        references twin_class
            on update cascade on delete cascade,
    extends_depth                integer default 0 not null,
    head_twin_pointer_id     uuid,
    field_finder_featurer_id     integer           not null
        references featurer,
    field_finder_params          hstore
);

create index if not exists face_tc001_face_id_idx
    on face_tc001 (face_id);

create index if not exists face_tc001_twin_pointer_validator_rule_id_idx
    on face_tc001 (twin_pointer_validator_rule_id);

create index if not exists face_tc001_class_selector_label_i18n_id_idx
    on face_tc001 (class_selector_label_i18n_id);

create index if not exists face_tc001_save_button_label_i18n_id_idx
    on face_tc001 (save_button_label_i18n_id);

create index if not exists face_tc001_header_i18n_id_idx
    on face_tc001 (header_i18n_id);

create index if not exists face_tc001_header_icon_resource_id_idx
    on face_tc001 (header_icon_resource_id);

create index if not exists face_tc001_twin_class_id_idx
    on face_tc001 (twin_class_id);

create index if not exists face_tc001_head_twin_pointer_id_idx
    on face_tc001 (head_twin_pointer_id);

create index if not exists face_tc001_field_finder_featurer_id_idx
    on face_tc001 (field_finder_featurer_id);


CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
select uuid_generate_v5('6ba7b810-9dad-11d1-80b4-00c04fd430c8', id::text) from face_tw004;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_page_pg001') THEN
            INSERT INTO face_pg001 (id, face_id, title_i18n_id, style_classes)
            SELECT uuid_generate_v5('00000000-0000-0000-0000-000000000001', f.face_id::text),
                   f.face_id,
                   f.title_i18n_id,
                   f.style_classes
            FROM face_page_pg001 f on conflict do nothing;
        END IF;
    END;
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_page_pg001_widget') THEN
            INSERT INTO face_pg001_widget (id, face_pg001_id, twin_pointer_validator_rule_id, widget_face_id, active, style_classes)
            SELECT f.id,
                   uuid_generate_v5('00000000-0000-0000-0000-000000000001', f.face_id::text),
                   null,
                   f.widget_face_id,
                   f.active,
                   f.style_classes
            FROM face_page_pg001_widget f on conflict do nothing;
        END IF;
    END;
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_page_pg002_layout') THEN
            INSERT INTO face_pg002_layout (id, description)
            SELECT f.id, f.description
            FROM face_page_pg002_layout f on conflict do nothing;
        END IF;
    END;
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_page_pg002') THEN
            INSERT INTO face_pg002 (id, face_id, twin_pointer_validator_rule_id, title_i18n_id, face_pg002_layout_id, style_classes)
            SELECT uuid_generate_v5('00000000-0000-0000-0000-000000000001', f.face_id::text),
                   f.face_id,
                   null,
                   f.title_i18n_id,
                   f.face_page_pg002_layout_id,
                   f.style_classes
            FROM face_page_pg002 f on conflict do nothing;
        END IF;
    END;
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_page_pg002_tab') THEN
            INSERT INTO face_pg002_tab (id, face_pg002_id, twin_pointer_validator_rule_id, icon_resource_id, title_i18n_id, active, style_classes, "order")
            SELECT f.id,
                   uuid_generate_v5('00000000-0000-0000-0000-000000000001', f.face_id::text),
                   null,
                   f.icon_resource_id,
                   f.title_i18n_id,
                   f.active,
                   f.style_classes,
                   f."order"
            FROM face_page_pg002_tab f on conflict do nothing;
        END IF;
    END;
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_page_pg002_widget') THEN
            INSERT INTO face_pg002_widget (id, face_pg002_tab_id, twin_pointer_validator_rule_id, widget_face_id, active, style_classes)
            SELECT f.id,
                   f.face_page_pg002_tab_id,
                   null,
                   f.widget_face_id,
                   f.active,
                   f.style_classes
            FROM face_page_pg002_widget f on conflict do nothing;
        END IF;
    END;
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_twidget_tw001') THEN
            INSERT INTO face_tw001 (id, face_id, twin_pointer_validator_rule_id, key, label_i18n_id, images_twin_class_field_id)
            SELECT uuid_generate_v5('00000000-0000-0000-0000-000000000001', f.face_id::text),
                   f.face_id,
                   null,
                   f.key,
                   f.label_i18n_id,
                   f.images_twin_class_field_id
            FROM face_twidget_tw001 f on conflict do nothing;
        END IF;
    END;
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_twidget_tw002') THEN
            INSERT INTO face_tw002 (id, face_id, twin_pointer_validator_rule_id, key, label_i18n_id, i18n_twin_class_field_id)
            SELECT uuid_generate_v5('00000000-0000-0000-0000-000000000001', f.face_id::text),
                   f.face_id,
                   null,
                   f.key,
                   f.label_i18n_id,
                   f.i18n_twin_class_field_id
            FROM face_twidget_tw002 f on conflict do nothing;
        END IF;
    END;
$$;

-- DO
-- $$
--     BEGIN
--         IF EXISTS (SELECT 1
--                    FROM information_schema.tables
--                    WHERE table_schema = 'public'
--                      AND table_name = 'face_twidget_tw002_accordion_item') THEN
--             INSERT INTO face_tw002_accordion_item (id, face_tw002_id, locale, label_i18n_id)
--             SELECT f.id,
--                    uuid_generate_v5('00000000-0000-0000-0000-000000000001', f.face_id::text),
--                    f.locale,
--                    f.label_i18n_id
--             FROM face_twidget_tw002_accordion_item f on conflict do nothing;
--         END IF;
--     END;
-- $$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_twidget_tw004') THEN
            INSERT INTO face_tw004 (id, face_id, twin_pointer_validator_rule_id, key, field_finder_featurer_id, field_finder_params,
                                   editable_field_filter_featurer_id, editable_field_filter_params)
            SELECT uuid_generate_v5('00000000-0000-0000-0000-000000000001', f.face_id::text),
                   f.face_id,
                   null,
                   f.key,
                   f.field_finder_featurer_id,
                   f.field_finder_params,
                   f.editable_field_filter_featurer_id,
                   f.editable_field_filter_params
            FROM face_twidget_tw004 f on conflict do nothing;
        END IF;
    END;
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_twidget_tw005') THEN
            INSERT INTO face_tw005 (id, face_id, twin_pointer_validator_rule_id, align_vertical, glue, style_classes)
            SELECT uuid_generate_v5('00000000-0000-0000-0000-000000000001', f.face_id::text),
                   f.face_id,
                   null,
                   f.align_vertical,
                   f.glue,
                   f.style_classes
            FROM face_twidget_tw005 f on conflict do nothing;
        END IF;
    END;
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_twidget_tw005_button') THEN
            INSERT INTO face_tw005_button (id, face_tw005_id, twin_pointer_validator_rule_id, twinflow_transition_id, label_i18n_id,
                                          icon_resource_id, "order", active, style_classes, show_when_inactive)
            SELECT f.id,
                   uuid_generate_v5('00000000-0000-0000-0000-000000000001', f.face_id::text),
                   null,
                   f.twinflow_transition_id,
                   f.label_i18n_id,
                   f.icon_resource_id,
                   f."order",
                   f.active,
                   f.style_classes,
                   f.show_when_inactive
            FROM face_twidget_tw005_button f on conflict do nothing;
        END IF;
    END;
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_widget_wt001') THEN
            INSERT INTO face_wt001 (id, face_id, twin_pointer_validator_rule_id, key, label_i18n_id, twin_class_id, search_id, show_create_button)
            SELECT uuid_generate_v5('00000000-0000-0000-0000-000000000001', f.face_id::text),
                   f.face_id,
                   null,
                   f.key,
                   f.label_i18n_id,
                   f.twin_class_id,
                   f.search_id,
                   f.show_create_button
            FROM face_widget_wt001 f on conflict do nothing;
        END IF;
    END;
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_widget_wt001_column') THEN
            INSERT INTO face_wt001_column (id, face_wt001_id, twin_pointer_validator_rule_id, twin_class_field_id, "order", label_i18n_id, show_by_default)
            SELECT f.id,
                   uuid_generate_v5('00000000-0000-0000-0000-000000000001', f.face_id::text),
                   null,
                   f.twin_class_field_id,
                   f."order",
                   f.label_i18n_id,
                   f.show_by_default
            FROM face_widget_wt001_column f on conflict do nothing;
        END IF;
    END;
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_widget_wt002') THEN
            INSERT INTO face_wt002 (id, face_id, twin_pointer_validator_rule_id, key, style_classes)
            SELECT uuid_generate_v5('00000000-0000-0000-0000-000000000001', f.face_id::text),
                   f.face_id,
                   null,
                   f.key,
                   f.style_classes
            FROM face_widget_wt002 f on conflict do nothing;
        END IF;
    END;
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_widget_wt002_button') THEN
            INSERT INTO face_wt002_button (id, face_wt002_id, twin_pointer_validator_rule_id, key, label_i18n_id, icon_resource_id, style_classes, modal_face_id)
            SELECT f.id,
                   uuid_generate_v5('00000000-0000-0000-0000-000000000001', f.face_id::text),
                   null,
                   f.key,
                   f.label_i18n_id,
                   f.icon_resource_id,
                   f.style_classes,
                   f.modal_face_id
            FROM face_widget_wt002_button f on conflict do nothing;
        END IF;
    END;
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'face_widget_wt003') THEN
            INSERT INTO face_wt003 (id, face_id, twin_pointer_validator_rule_id, level, title_i18n_id, message_i18n_id, icon_resource_id, style_classes)
            SELECT uuid_generate_v5('00000000-0000-0000-0000-000000000001', f.face_id::text),
                   f.face_id,
                   null,
                   f.level,
                   f.title_i18n_id,
                   f.message_i18n_id,
                   f.icon_resource_id,
                   f.style_classes
            FROM face_widget_wt003 f on conflict do nothing;
        END IF;
    END;
$$;
