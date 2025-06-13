create table if not exists template_generator
(
    id                     uuid                                          not null
        constraint template_generator_pk
            primary key,
    domain_id        uuid
        constraint template_generator_domain_id_fk
            references domain
            on update cascade on delete cascade,
    name                   varchar(50),
    description            varchar(255),
    templater_featurer_id    integer
        constraint template_generator_emailer_featurer_id_fk
            references featurer
            on update cascade,
    templater_params         hstore,
    active boolean not null default true,
    created_at             timestamp default CURRENT_TIMESTAMP
);

create index if not exists template_generator_owner_domain_id_index
    on template_generator (domain_id);
create index if not exists template_generator_templater_featurer_id_index
    on template_generator (templater_featurer_id);

INSERT INTO featurer_type (id, name) VALUES (37, 'Templator') on conflict (id) do nothing;
INSERT INTO featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (3701, 37, '', '', '', false) on conflict (id) do nothing;
INSERT INTO template_generator (id, domain_id, name, description, templater_featurer_id, templater_params, active, created_at)
VALUES ('00000000-0000-0000-0010-000000000001', null, 'Simple', 'Simple template generator', 3701, null::hstore, true, now()) on conflict do nothing ;

create table if not exists event
(
    id                  uuid not null
        constraint event_pk
            primary key,
    key                 varchar(100)
-- add more fields in future
);

INSERT INTO event (id, key) VALUES ('00000000-0000-0002-0001-000000000001', 'SIGNUP_EMAIL_VERIFICATION_CODE') on conflict (id) do nothing;
INSERT INTO event (id, key) VALUES ('00000000-0000-0002-0001-000000000002', 'SIGNUP_EMAIL_VERIFICATION_LINK') on conflict (id) do nothing;

DO $$
    BEGIN
        -- Удалить таблицу email_sender, только если есть колонка owner_domain_id
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'email_sender'
              AND column_name = 'owner_domain_id'
        ) THEN
            EXECUTE 'DROP TABLE IF EXISTS public.email_sender';
            EXECUTE 'DROP TABLE IF EXISTS public.email_sender_status';
        END IF;
    END $$;


create table if not exists email_sender
(
    id                  uuid                   not null
        constraint email_sender_pk
            primary key,
    domain_id     uuid
        constraint email_sender_domain_id_fk
            references domain
            on update cascade on delete cascade,
    name                varchar(50),
    description         varchar(255),
    emailer_featurer_id integer
        constraint email_sender_emailer_featurer_id_fk
            references featurer
            on update cascade,
    emailer_params      hstore,
    src_email           varchar not null ,
    active              boolean   default true not null,
    created_at          timestamp default CURRENT_TIMESTAMP
);

INSERT INTO email_sender (id, domain_id, src_email, name, description, emailer_featurer_id, emailer_params, created_at, src_email, active)
VALUES ('00000000-0000-0000-0009-000000000001', null, '', 'Internal', 'System internal email sender ', 3301, null, '2025-05-25 16:49:12.539132', null, true) on conflict (id) do nothing;


create index if not exists email_sender_emailer_featurer_id_index
    on email_sender (emailer_featurer_id);

create index if not exists email_sender_domain_id_index
    on email_sender (domain_id);

create table if not exists notification_email
(
    id                  uuid not null
        constraint notification_email_pk
            primary key,
    domain_id    uuid
        constraint notification_email_domain_id_fk
            references domain
            on update cascade on delete cascade,
    event_id   uuid
        constraint notification_email_event_id_fk
            references event
            on update cascade on delete cascade,
    email_sender_id      uuid not null
        constraint notification_email_email_sender_id_fk
            references email_sender
            on update cascade on delete cascade,
    subject_i18n_id uuid not null
        constraint notification_email_subject_i18n_id_fk
            references i18n
            on update cascade,
    subject_template_generator_id  uuid
        constraint notification_email_subject_template_generator_id_fk
            references template_generator
            on update cascade on delete cascade,
    body_i18n_id        uuid not null
        constraint notification_email_body_i18n_id_fk
            references i18n
            on update cascade,
    body_template_generator_id  uuid
        constraint notification_email_body_template_generator_id_fk
            references template_generator
            on update cascade on delete cascade,
    active boolean not null default true,
    created_at           timestamp default CURRENT_TIMESTAMP
);

create index if not exists notification_email_domain_id_index
    on notification_email (domain_id);

create index if not exists notification_email_email_sender_id_index
    on notification_email (email_sender_id);

create index if not exists notification_email_subject_template_generator_idindex
    on notification_email (subject_template_generator_id);

create index if not exists notification_email_body_template_generator_idindex
    on notification_email (body_template_generator_id);

create index if not exists notification_email_body_i18n_id_index
    on notification_email (body_i18n_id);

create index if not exists notification_email_body_i18n_id_index
    on notification_email (subject_i18n_id);

create unique index if not exists notification_email_domain_id_event_id_uindex
    on notification_email (domain_id, event_id);

INSERT INTO i18n_type (id, name) VALUES ('notificationEmailSubject', 'notification email subject') on conflict (id) do nothing;
INSERT INTO i18n_type (id, name) VALUES ('notificationEmailBody', 'notification email body') on conflict (id) do nothing;

insert into i18n (id, name, key, i18n_type_id) VALUES
        ('c84ff233-d986-4eba-a398-080a70966865', 'Signup verification by code: default subject', null, 'notificationEmailSubject'),
        ('1e6cc220-b513-47a4-8ad4-2850302ffa9a', 'Signup verification by code: default body', null, 'notificationEmailBody')
                                        on conflict (id) do update set name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;

insert into i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
        ('c84ff233-d986-4eba-a398-080a70966865', 'en', 'Welcome to ${domain.name}', 0),
        ('1e6cc220-b513-47a4-8ad4-2850302ffa9a', 'en', 'Your email verification code: ${email.verification.code}', 0)
                                        on conflict (i18n_id,locale) do update set translation = excluded.translation, usage_counter = excluded.usage_counter;;

INSERT INTO notification_email (id, domain_id, event_id, email_sender_id, subject_i18n_id, subject_template_generator_id, body_i18n_id, body_template_generator_id, active, created_at)
VALUES ('00000000-0000-0000-0011-000000000001', null, '00000000-0000-0002-0001-000000000001', '00000000-0000-0000-0009-000000000001', 'c84ff233-d986-4eba-a398-080a70966865', '00000000-0000-0000-0010-000000000001', '1e6cc220-b513-47a4-8ad4-2850302ffa9a', '00000000-0000-0000-0010-000000000001', true, DEFAULT)
on conflict (id) do nothing;