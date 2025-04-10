create table if not exists face_twidget_tw005
(
    face_id                   uuid        not null
        constraint face_twidget_tw005_face_id_fk
            primary key
        references face
            on update cascade on delete cascade ,
    align_vertical boolean default false not null,
    glue boolean default false not null,
    style_attributes      hstore
);

create table if not exists face_twidget_tw005_button
(
    id             uuid                 not null
        constraint face_twidget_tw005_button_pk
            primary key,
    face_id        uuid                 not null
        constraint face_twidget_tw005_button_face_id_fk
            references face
            on update cascade on delete cascade,
    twinflow_transition_id uuid                 not null
        constraint face_twidget_tw005_button_twinflow_transition_id_fk
            references twinflow_transition
            on update cascade on delete cascade,
    label_i18n_id             uuid
        constraint face_twidget_tw005_button_label_i18n_id_fk
            references i18n
            on update cascade,
    icon_resource_id                  uuid
        constraint face_twidget_tw005_button_icon_resource_id_fk
            references resource
            on update cascade,
    style_attributes      hstore,
    "order"       integer default 1    not null,
    active         boolean default true not null
);

insert into face_component values ('TW005', 'TWIDGET', 'Transitions perform buttons')  on conflict do nothing ;