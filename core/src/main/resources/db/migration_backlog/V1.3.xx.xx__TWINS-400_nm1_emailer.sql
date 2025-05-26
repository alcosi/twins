create table if not exists email_sender_status
(
    id          varchar not null
        primary key,
    description text
);

INSERT INTO email_sender_status (id, description)
VALUES ('ACTIVE', 'Active status'),
       ('DISABLED', 'Disabled status')
on conflict (id) do nothing;

create table if not exists email_sender
(
    id                        uuid                                          not null
        constraint email_sender_pk
            primary key,
    owner_domain_id                         uuid
        constraint email_sender_owner_domain_id_fk
            references domain
            on update cascade on delete cascade ,
    name                      varchar(50),
    description               varchar(255),
    emailer_featurer_id integer
        constraint email_sender_emailer_featurer_id_fk
            references featurer
            on update cascade,
    emailer_params      hstore,
    email_sender_status_id             varchar   default 'ACTIVE'::character varying not null
        constraint email_sender_status_fk
            references email_sender_status,
    created_at                timestamp default CURRENT_TIMESTAMP
);

insert into featurer(id, featurer_type_id, class, name, description)
values (3301, 33, '', '', '')
on conflict (id) do nothing;


INSERT INTO email_sender (id, name, description, emailer_featurer_id, emailer_params, email_sender_status_id, created_at)
VALUES ('00000000-0000-0000-0009-000000000001', 'Internal', 'System internal email sender ', 3301, 'host => "127.0.0.1, port => 587, username => "username", password => "password", auth => true, starttls => true', 'ACTIVE', now()) on conflict do nothing;




