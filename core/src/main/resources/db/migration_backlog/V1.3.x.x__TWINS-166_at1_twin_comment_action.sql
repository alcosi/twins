-- create table twin_comment_action
create table if not exists twin_comment_action (
    id varchar(20) not null
        constraint twin_comment_action_pk
            primary key
);

INSERT INTO public.twin_comment_action (id) VALUES ('EDIT') on conflict on constraint twin_comment_action_pk do nothing ;
INSERT INTO public.twin_comment_action (id) VALUES ('DELETE') on conflict on constraint twin_comment_action_pk do nothing ;
INSERT INTO public.twin_comment_action (id) VALUES ('PIN') on conflict on constraint twin_comment_action_pk do nothing ;
INSERT INTO public.twin_comment_action (id) VALUES ('UNPIN') on conflict on constraint twin_comment_action_pk do nothing ;
INSERT INTO public.twin_comment_action (id) VALUES ('VOTE') on conflict on constraint twin_comment_action_pk do nothing ;
INSERT INTO public.twin_comment_action (id) VALUES ('REACT') on conflict on constraint twin_comment_action_pk do nothing ;
INSERT INTO public.twin_comment_action (id) VALUES ('HIDE') on conflict on constraint twin_comment_action_pk do nothing ;
INSERT INTO public.twin_comment_action (id) VALUES ('UNHIDE') on conflict on constraint twin_comment_action_pk do nothing ;

-- create table twin_comment_action_alien_permission
create table if not exists twin_comment_action_alien_permission
(
    id                     uuid        not null
        constraint twin_comment_action_alien_permission_pk
            primary key,
    twin_class_id          uuid        not null
        constraint twin_comment_action_alien_permission_twin_class_id_fk
            references twin_class
            on update cascade,
    twin_comment_action_id varchar(20) not null
        constraint twin_comment_action_alien_permission_twin_comment_action_id_fk
            references twin_comment_action
            on update cascade,
    permission_id          uuid        not null
        constraint twin_comment_action_alien_permission_permission_id_fk
            references permission
            on update cascade
);

create index if not exists twin_comment_action_alien_permission_twin_class_id_idx
    on twin_comment_action_alien_permission (twin_class_id);

create index if not exists twin_comment_action_alien_permission_twin_comment_action_id_idx
    on twin_comment_action_alien_permission (twin_comment_action_id);


-- create table twin_comment_action_alien_validator
create table if not exists twin_comment_action_alien_validator
(
    id                         uuid                  not null
        constraint twin_comment_action_alien_validator_pk
            primary key,
    twin_class_id              uuid                  not null
        constraint twin_comment_action_alien_validator_twin_class_id_fk
            references twin_class
            on update cascade,
    twin_comment_action_id     varchar(20) not null ,
    "order"                    integer default 1,
    twin_validator_featurer_id integer               not null
        constraint twin_comment_action_alien_validator_featurer_id_fk
            references featurer
            on update cascade,
    twin_validator_params      hstore,
    invert                     boolean default false not null,
    active                     boolean default true  not null
);

create index if not exists twin_comment_action_alien_valid_twin_validator_featurer_id_idx
    on twin_comment_action_alien_validator (twin_validator_featurer_id);

create index if not exists twin_comment_action_alien_validator_twin_class_id_idx
    on twin_comment_action_alien_validator (twin_class_id);

create index if not exists twin_comment_action_alien_validator_twin_comment_action_id_idx
    on twin_comment_action_alien_validator (twin_comment_action_id);

create unique index if not exists twin_comment_action_alien_validator_order_uniq
    on twin_comment_action_alien_validator (twin_class_id, twin_comment_action_id, "order");


--create table twin_comment_action_self
create table if not exists twin_comment_action_self
(
    id                              uuid        not null
        constraint twin_comment_action_self_pk
            primary key,
    twin_class_id                   uuid        not null
        constraint twin_comment_action_self_twin_class_id_fk
            references twin_class,
    restrict_twin_comment_action_id varchar(20) not null
        constraint twin_comment_action_self_twin_comment_action_id_fk
            references twin_comment_action
);

create index if not exists twin_comment_action_self_twin_class_id_idx
    on twin_comment_action_self (twin_class_id);


-- rename table twin_class_action_permission → twin_action_permission
alter index if exists twin_class_action_permission_twin_class_id_index rename to twin_action_permission_twin_class_id_index;

alter table if exists twin_class_action_permission
    rename constraint twin_class_action_permission_pk to twin_action_permission_pk;

alter table if exists twin_class_action_permission
    rename constraint twin_class_action_permission_permission_id_fk to twin_action_permission_permission_id_fk;

alter table if exists twin_class_action_permission
    rename constraint twin_class_action_permission_twin_class_id_fk to twin_action_permission_twin_class_id_fk;

alter table if exists twin_class_action_permission
    rename to twin_action_permission;



-- rename table twin_class_action_validator → twin_action_validator
alter index if exists twin_class_action_validator_twin_action_id_index rename to twin_action_validator_twin_action_id_index;

alter index if exists twin_class_action_validator_twin_class_id_index rename to twin_action_validator_twin_class_id_index;

alter table if exists twin_class_action_validator
    rename constraint twin_class_action_validator_pk to twin_action_validator_pk;

alter table if exists twin_class_action_validator
    rename constraint twin_class_action_validator_twin_class_id_fk to twin_action_validator_twin_class_id_fk;

alter table if exists twin_class_action_validator
    rename to twin_action_validator;
