alter table link
    add if not exists linker_featurer_id integer
        constraint link_featurer_id_fk
            references public.featurer;

alter table link
    add if not exists linker_params hstore;

INSERT INTO public.featurer_type (id, name, description)
VALUES (30, 'Linker', '')
on conflict (id) do nothing;

insert into public.featurer(id, featurer_type_id, class, name, description)
values (3001, 30, '', '', '')
on conflict (id) do nothing;

update link
set linker_featurer_id = 3001
where linker_featurer_id is null;

alter table link
    alter column linker_featurer_id set not null;