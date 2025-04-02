INSERT INTO public.featurer_type VALUES (31, 'Pointer', 'Point from given twin to some other twin (linked, head or some other logic)') on conflict do nothing ;

INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (3101, 31, '', '', '', false) on conflict (id) do nothing;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (3102, 31, '', '', '', false) on conflict (id) do nothing;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (3103, 31, '', '', '', false) on conflict (id) do nothing;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (3104, 31, '', '', '', false) on conflict (id) do nothing;

create table if not exists face_twidget
(
    face_id                     uuid    not null
        constraint face_twidget_face_id_fk
            primary key
        references face
            on update cascade on delete cascade,
    pointer_featurer_id integer              not null
        constraint face_twidget_pointer_featurer_id_fk
            references featurer
            on update cascade on delete restrict ,
    pointer_params      hstore
);

create table if not exists face_twidget_tw004
(
    face_id                     uuid    not null
        constraint face_twidget_tw004_face_id_fk
            primary key
        references face
            on update cascade on delete cascade ,
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

-- cascade deletion for face
alter table public.face_navbar_nb001
    drop constraint face_navbar_nb001_face_id_fk;
alter table public.face_navbar_nb001
    add constraint face_navbar_nb001_face_id_fk
        foreign key (face_id) references public.face
            on update cascade on delete cascade;

alter table public.face_navbar_nb001_menu_items
    drop constraint face_navbar_nb001_menu_items_face_id_fk;
alter table public.face_navbar_nb001_menu_items
    add constraint face_navbar_nb001_menu_items_face_id_fk
        foreign key (face_id) references public.face
            on update cascade on delete cascade;

alter table public.face_navbar_nb001_menu_items
    drop constraint face_navbar_nb001_menu_items_target_page_face_id_fk;
alter table public.face_navbar_nb001_menu_items
    add constraint face_navbar_nb001_menu_items_target_page_face_id_fk
        foreign key (target_page_face_id) references public.face
            on update cascade on delete cascade;

alter table public.face_page_pg001
    drop constraint face_page_pg001_face_id_fkey;
alter table public.face_page_pg001
    add foreign key (face_id) references public.face
        on update cascade on delete cascade;

alter table public.face_page_pg001_widget
    drop constraint face_page_pg001_widget_widget_face_id_fk;
alter table public.face_page_pg001_widget
    add constraint face_page_pg001_widget_widget_face_id_fk
        foreign key (widget_face_id) references public.face
            on update cascade on delete cascade;

alter table public.face_page_pg002_widget
    drop constraint face_page_pg002_widget_widget_face_id_fk;
alter table public.face_page_pg002_widget
    add constraint face_page_pg002_widget_widget_face_id_fk
        foreign key (widget_face_id) references public.face
            on update cascade on delete cascade;

