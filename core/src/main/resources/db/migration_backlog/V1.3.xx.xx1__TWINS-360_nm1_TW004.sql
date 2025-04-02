INSERT INTO public.featurer_type VALUES (31, 'Pointer', 'Point from given twin to some other twin (linked, head or some other logic)');

INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (3101, 31, '', '', '', false) on conflict (id) do nothing;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (3102, 31, '', '', '', false) on conflict (id) do nothing;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (3103, 31, '', '', '', false) on conflict (id) do nothing;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (3104, 31, '', '', '', false) on conflict (id) do nothing;

create table if not exists face_twidget_tw004
(
    face_id                     uuid    not null
        constraint face_twidget_tw004_face_id_fk
            primary key
        references face
            on update cascade on delete restrict,
    pointer_featurer_id integer              not null
        constraint face_twidget_tw004_pointer_featurer_id_fk
            references featurer
            on update cascade on delete restrict ,
    pointer_params      hstore,
    key                     varchar not null,
    label_i18n_id               uuid
        constraint face_twidget_tw004_label_i18n_id_fk
            references i18n
            on update cascade,
    twin_class_field_id               uuid
        constraint face_twidget_tw004_twin_class_field_id_fk
            references twin_class_field
            on update cascade on delete cascade
);

insert into face_component values ('TW004', 'TWIDGET', 'Twidget to display single twin field') on conflict do nothing ;
