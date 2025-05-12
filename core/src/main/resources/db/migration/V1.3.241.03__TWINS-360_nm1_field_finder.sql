alter table face_twidget_tw004
    add if not exists field_finder_featurer_id integer
        constraint face_twidget_tw004_field_finder_featurer_id_fk
            references public.featurer;

alter table face_twidget_tw004
    add if not exists field_finder_params hstore;

INSERT INTO public.featurer_type (id, name, description)
VALUES (32, 'FieldFinder', '')
on conflict (id) do nothing;

insert into public.featurer(id, featurer_type_id, class, name, description)
values (3202, 32, '', '', '')
on conflict (id) do nothing;

update face_twidget_tw004
set field_finder_featurer_id = 3202, field_finder_params = hstore('fieldIds', twin_class_field_id::text) || hstore('excludeGivenIds', 'false')
where field_finder_featurer_id is null;

alter table face_twidget_tw004
    alter column field_finder_featurer_id set not null;

alter table public.face_twidget_tw004
    drop column if exists label_i18n_id;

alter table public.face_twidget_tw004
    drop column if exists twin_class_field_id;