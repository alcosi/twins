// todo drop 4 entries
drop table if exists twin_attachment_action_alien_permission;
drop table if exists twin_attachment_action_alien_validator_rule;
drop table if exists twin_attachment_action_self;
drop table if exists twin_attachment_action;


-- create table twin_attachment_action
create table if not exists twin_attachment_action (
    id varchar(20) not null
        constraint twin_attachment_action_pk
            primary key
    );

INSERT INTO public.twin_attachment_action (id) VALUES ('VIEW') on conflict on constraint twin_attachment_action_pk do nothing ;
INSERT INTO public.twin_attachment_action (id) VALUES ('EDIT') on conflict on constraint twin_attachment_action_pk do nothing ;
INSERT INTO public.twin_attachment_action (id) VALUES ('DELETE') on conflict on constraint twin_attachment_action_pk do nothing ;

-- create table twin_attachment_action_alien_permission
create table if not exists twin_attachment_action_alien_permission (
    id                     uuid        not null
        constraint twin_attachment_action_alien_permission_pk
            primary key,
    twin_class_id          uuid
        constraint twin_attachment_action_alien_permission_twin_class_id_fk
        references twin_class
            on update cascade,
    twin_attachment_action_id varchar(20) not null
        constraint twin_attachment_action_alien_twin_attachment_action_id_fk
        references twin_comment_action
            on update cascade,
    permission_id          uuid        not null
        constraint twin_attachment_action_alien_permission_permission_id_fk
        references permission
            on update cascade
    );

create index if not exists twin_attachment_action_alien_permission_twin_class_id_idx
    on twin_attachment_action_alien_permission (twin_class_id);

create index if not exists twin_attachment_action_id_idx
    on twin_attachment_action_alien_permission (twin_attachment_action_id);


-- create table twin_attachment_action_alien_validator_rule
create table if not exists twin_attachment_action_alien_validator_rule (
    id                         uuid                  not null
        constraint twin_attachment_action_alien_validator_rule_pk
            primary key,
    twin_class_id              uuid                  not null
        constraint twin_attachment_action_alien_validator_rule_twin_class_id_fk
        references twin_class
        on update cascade,
    "order" integer default 1,
    active boolean default true,
    twin_attachment_action_id varchar(20) not null
        constraint twin_attachment_action_alien_validator_rule_attach_action_id_fk
            references twin_attachment_action
            on update cascade,
    twin_validator_set_id uuid not null
        constraint twin_attachment_action_alien_validator_rule_validator_set_fk
        references twin_validator_set
        on update cascade
    );

create index if not exists twin_attachment_action_alien_validator_rule_twin_class_idx
    on twin_attachment_action_alien_validator_rule (twin_class_id);

create index if not exists twin_attachment_action_alien_validator_rule_attach_action_idx
    on twin_attachment_action_alien_validator_rule (twin_attachment_action_id);

create unique index if not exists twin_attachment_action_alien_validator_rule_order_uniq
    on twin_attachment_action_alien_validator_rule (twin_class_id, twin_attachment_action_id, "order");


--create table twin_attachment_action_self
create table if not exists twin_attachment_action_self (
    id                              uuid        not null
        constraint twin_attachment_action_self_pk
            primary key,
    twin_class_id                   uuid        not null
        constraint twin_attachment_action_self_twin_class_id_fk
        references twin_class,
    restrict_twin_attachment_action_id varchar(20) not null
        constraint twin_attachment_action_self_twin_attachment_action_id_fk
        references twin_comment_action,
    twin_validator_set_id uuid not null
        constraint twin_attachment_action_self_twin_validator_set_id_fk
        references twin_validator_set
        on update cascade
    );

create index if not exists twin_attachment_action_self_twin_class_id_idx
    on twin_attachment_action_self (twin_class_id);
