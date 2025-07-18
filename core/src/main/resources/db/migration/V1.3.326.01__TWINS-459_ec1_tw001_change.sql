alter table face_tw001
    add column if not exists uploadable boolean not null default false;
