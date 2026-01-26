create table if not exists twinflow_factory
(
    id                       uuid
        constraint twinflow_factory_pk
            primary key,
    twinflow_id              uuid        not null
        constraint twinflow_factory_twinflow_id_fk
            references twinflow
            on update cascade on delete cascade,
    twin_factory_launcher_id varchar(40) not null
        constraint twinflow_factory_twin_factory_launcher_id_fk
            references twin_factory_launcher
            on update cascade on delete cascade,
    twin_factory_id          uuid        not null
        constraint twinflow_factory_twin_factory_id_fk
            references twin_factory
            on update cascade on delete restrict
);

create index if not exists twinflow_factory_twin_factory_id_index
    on twinflow_factory (twin_factory_id);

create unique index if not exists twinflow_factory_twin_factory_launcher_id_twinflow_id_uindex
    on twinflow_factory (twin_factory_launcher_id, twinflow_id);


update twin_factory_launcher set id = 'onTwinCreate' where id = 'beforeTwinCreate';
update twin_factory_launcher set id = 'onTwinUpdate' where id = 'beforeTwinUpdate';
update twin_factory_launcher set id = 'onSketchCreate' where id = 'beforeTwinSketch';
insert into twin_factory_launcher values ('onSketchUpdate') on conflict do nothing ;
insert into twin_factory_launcher values ('onSketchFinalize') on conflict do nothing ;

update twin_factory_launcher set id = 'afterSketchCreate' where id = 'afterTwinSketch';
insert into twin_factory_launcher values ('afterSketchUpdate') on conflict do nothing ;
insert into twin_factory_launcher values ('afterSketchFinalize') on conflict do nothing ;



DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'twinflow'
              AND column_name = 'before_create_twin_factory_id'
        ) THEN
            insert into twinflow_factory (id, twinflow_id, twin_factory_launcher_id, twin_factory_id)
                select uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, concat(id, before_create_twin_factory_id)::text), id, 'onTwinCreate', before_create_twin_factory_id
                from twinflow where before_create_twin_factory_id is not null on conflict do nothing;
            alter table public.twinflow
                drop column before_create_twin_factory_id;
        END IF;
    END
$$;

DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'twinflow'
              AND column_name = 'before_update_twin_factory_id'
        ) THEN
            insert into twinflow_factory (id, twinflow_id, twin_factory_launcher_id, twin_factory_id)
                select uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, concat(id, before_update_twin_factory_id)::text), id, 'onTwinUpdate', before_update_twin_factory_id
                from twinflow where before_update_twin_factory_id is not null on conflict do nothing;
            alter table public.twinflow
                drop column before_update_twin_factory_id;
        END IF;
    END
$$;


DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'twinflow'
              AND column_name = 'before_sketch_twin_factory_id'
        ) THEN
            insert into twinflow_factory (id, twinflow_id, twin_factory_launcher_id, twin_factory_id)
                select uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, concat(id, before_sketch_twin_factory_id)::text), id, 'onSketchCreate', before_sketch_twin_factory_id
                from twinflow where before_sketch_twin_factory_id is not null on conflict do nothing;
            alter table public.twinflow
                drop column before_sketch_twin_factory_id;
        END IF;
    END
$$;


DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'twinflow'
              AND column_name = 'after_create_twin_factory_id'
        ) THEN
            insert into twinflow_factory (id, twinflow_id, twin_factory_launcher_id, twin_factory_id)
                select uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, concat(id, after_create_twin_factory_id)::text), id, 'afterTwinCreate', after_create_twin_factory_id
                from twinflow where after_create_twin_factory_id is not null on conflict do nothing;
            alter table public.twinflow
                drop column after_create_twin_factory_id;
        END IF;
    END
$$;


DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'twinflow'
              AND column_name = 'after_update_twin_factory_id'
        ) THEN
            insert into twinflow_factory (id, twinflow_id, twin_factory_launcher_id, twin_factory_id)
                select uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, concat(id, after_update_twin_factory_id)::text), id, 'afterTwinUpdate', after_update_twin_factory_id
                from twinflow where after_update_twin_factory_id is not null on conflict do nothing;
            alter table public.twinflow
                drop column after_update_twin_factory_id;
        END IF;
    END
$$;


DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'twinflow'
              AND column_name = 'after_sketch_twin_factory_id'
        ) THEN
            insert into twinflow_factory (id, twinflow_id, twin_factory_launcher_id, twin_factory_id)
                select uuid_generate_v5('00000000-0000-0000-0000-000000000001'::uuid, concat(id, after_sketch_twin_factory_id)::text), id, 'afterSketchUpdate', after_sketch_twin_factory_id
                from twinflow where after_sketch_twin_factory_id is not null on conflict do nothing;
            alter table public.twinflow
                drop column after_sketch_twin_factory_id;
        END IF;
    END
$$;


