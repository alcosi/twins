alter table public.face_page_pg002_tab
    add if not exists "order" smallint default 0 not null;
