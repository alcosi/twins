alter table if exists twin_field rename to twin_field_simple;
alter index if exists twin_field_pk rename to twin_field_simple_pk;
alter index if exists twin_field_twin_id_twin_class_field_id_uindex rename to twin_field_simple_twin_id_twin_class_field_id_uindex;

alter table twin_field_simple drop constraint if exists twin_field_twin_class_field_id_fk;
alter table twin_field_simple drop constraint if exists twin_field_twin_id_fk;
alter table twin_field_simple drop constraint if exists twin_field_simple_twin_id_fk;
alter table twin_field_simple drop constraint if exists twin_field_simple_twin_class_field_id_fk;

alter table twin_field_simple
    add constraint twin_field_simple_twin_id_fk
        foreign key (twin_id) references twin
            on update cascade on delete cascade;

alter table twin_field_simple
    add constraint twin_field_simple_twin_class_field_id_fk
        foreign key (twin_class_field_id) references twin_class_field
            on update cascade on delete cascade;

DO $$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_attribute WHERE attrelid = 'twin_field_data_list'::regclass AND attname = 'twin_class_field_id' AND attnum > 0 AND NOT attisdropped) THEN
            alter table twin_field_data_list drop constraint if exists twin_field_data_list_pk;
            alter table twin_field_data_list drop constraint if exists twin_field_data_list_data_list_option_id_fk;
            alter table twin_field_data_list drop constraint if exists twin_field_data_list_twin_field_id_fk;
            alter table twin_field_data_list drop constraint if exists twin_field_data_list_twin_id_fk;
            drop index if exists twin_field_data_list_pk;
            drop index if exists twin_field_data_list_data_list_option_id_fk;
            drop index if exists twin_field_data_list_twin_field_id_fk;
            drop index if exists twin_field_data_list_twin_id_fk;
            drop index if exists twin_field_data_list_data_list_option_id_index;
            drop index if exists twin_field_data_list_twin_field_id_index;
            drop index if exists twin_field_data_list_twin_id_index;

            -- simple backup for some time. it must be deleted in future
            alter table if exists twin_field_data_list rename to twin_field_data_list_old;

            create table twin_field_data_list
            (
                id                  uuid not null
                    constraint twin_field_data_list_pk
                        primary key,
                twin_id             uuid not null
                    constraint twin_field_data_list_twin_id_fk
                        references twin
                        on update cascade on delete cascade,
                twin_class_field_id uuid not null
                    constraint twin_field_data_list_twin_class_field_id_fk
                        references twin_class_field
                        on update cascade on delete cascade,
                data_list_option_id uuid not null
                    constraint twin_field_data_list_data_list_option_id_fk
                        references data_list_option
                        on update cascade on delete cascade
            );

            create index twin_field_data_list_twin_id_index on twin_field_data_list (twin_id);
            create index twin_field_data_list_data_list_option_id_index on twin_field_data_list (data_list_option_id);
            create index twin_field_data_list_twin_class_field_id_index on twin_field_data_list (twin_class_field_id);

            insert into twin_field_data_list (id, twin_id, twin_class_field_id, data_list_option_id) select tfdlo.id, tf.twin_id, tf.twin_class_field_id, tfdlo.data_list_option_id from twin_field_data_list_old tfdlo, twin_field_simple tf where tfdlo.twin_field_id = tf.id;
        END IF;
    END $$;


DO $$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_attribute WHERE attrelid = 'twin_field_user'::regclass AND attname = 'twin_class_field_id' AND attnum > 0 AND NOT attisdropped) THEN
            alter table twin_field_user drop constraint if exists twin_field_user_pk;
            alter table twin_field_user drop constraint if exists twin_field_user_user_id_fk;
            alter table twin_field_user drop constraint if exists twin_field_user_twin_field_id_fk;
            alter table twin_field_user drop constraint if exists twin_field_user_twin_id_fk;
            drop index if exists twin_field_user_pk;
            drop index if exists twin_field_user_user_id_fk;
            drop index if exists twin_field_user_twin_field_id_fk;
            drop index if exists twin_field_user_twin_id_fk;

            -- simple backup for some time. it must be deleted in future
            alter table if exists twin_field_user rename to twin_field_user_old;

            create table twin_field_user
            (
                id                  uuid not null
                    constraint twin_field_user_pk
                        primary key,
                twin_id             uuid not null
                    constraint twin_field_user_twin_id_fk
                        references twin
                        on update cascade on delete cascade,
                twin_class_field_id uuid not null
                    constraint twin_field_user_twin_class_field_id_fk
                        references twin_class_field
                        on update cascade on delete cascade,
                user_id uuid not null
                    constraint twin_field_user_user_id_fk
                        references "user"
                        on update cascade on delete cascade
            );

            create index if not exists twin_field_user_twin_id_index on twin_field_user (twin_id);
            create index if not exists twin_field_user_user_id_index on twin_field_user (user_id);
            create index if not exists twin_field_user_twin_class_field_id_index on twin_field_user (twin_class_field_id);

            insert into twin_field_user (id, twin_id, twin_class_field_id, user_id) select tfdlo.id, tf.twin_id, tf.twin_class_field_id, tfdlo.user_id from twin_field_user_old tfdlo, twin_field_simple tf where tfdlo.twin_field_id = tf.id;
        END IF;
    END $$;

drop table if exists twin_field_history;






