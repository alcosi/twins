create table if not exists user_group_map_type1
(
    id               uuid not null
        primary key,
    user_group_id    uuid not null
        constraint user_group_map_type1_user_group_id_fk
            references user_group
            on update cascade on delete cascade,
    user_id          uuid not null
        constraint user_group_map_type1_user_id_fk
            references "user"
            on update cascade on delete cascade,
    added_at         timestamp default CURRENT_TIMESTAMP,
    added_by_user_id uuid
        constraint user_group_map_type1_added_user_id_fk
            references "user"
            on update cascade
);

drop index if exists user_group_map_type1_user_id_user_group_id_uindex;

create unique index user_group_map_type1_user_id_user_group_id_uindex
    on user_group_map_type1 (user_id, user_group_id);



create table if not exists user_group_map_type2
(
    id                  uuid not null
        primary key,
    user_group_id       uuid not null
        constraint user_group_map_type2_user_group_id_fk
            references user_group
            on update cascade on delete cascade,
    business_account_id uuid not null
        constraint user_group_map_type2_business_account_id_fk
            references business_account
            on update cascade on delete cascade,
    user_id             uuid not null
        constraint user_group_map_type2_user_id_fk
            references "user"
            on update cascade on delete cascade,
    added_at            timestamp default CURRENT_TIMESTAMP,
    added_by_user_id    uuid
        constraint user_group_map_type2_added_user_id_fk
            references "user"
            on update cascade
);

drop index if exists user_group_map_type2_user_id_business_account_id_index;

create unique index if not exists user_group_map_type2_user_id_business_account_id_index
    on user_group_map_type2 (user_id, business_account_id, user_group_id);



create table if not exists user_group_map_type3
(
    id               uuid not null
        primary key,
    user_group_id    uuid not null
        constraint user_group_map_type3_user_group_id_fk
            references user_group
            on update cascade on delete cascade,
    domain_id        uuid not null
        constraint user_group_map_type3_business_account_id_fk
            references domain
            on update cascade on delete cascade,
    user_id          uuid not null
        constraint user_group_map_type3_user_id_fk
            references "user"
            on update cascade on delete cascade,
    added_at         timestamp default CURRENT_TIMESTAMP,
    added_by_user_id uuid
        constraint user_group_map_type3_added_user_id_fk
            references "user"
            on update cascade
);

drop index if exists user_group_map_type3_user_id_business_account_id_index;

create unique index if not exists user_group_map_type3_user_id_business_account_id_index
    on user_group_map_type3 (user_id, domain_id, user_group_id);


insert into user_group_map_type2
select *
from user_group_map
where business_account_id is not null
on conflict do nothing;

insert into featurer(id, featurer_type_id, class, name, description)
values (2005, 20, '', '', '')
on conflict (id) do nothing;

INSERT INTO user_group_type (id, name, slugger_featurer_id, slugger_params)
VALUES ('systemScopeDomainManage', null, 2005, null)
on conflict (id) do nothing;

INSERT INTO public.i18n (id, name, key, i18n_type_id)
VALUES ('00000000-0000-0001-0006-000000000001', 'user_group[domain_admin]', null, 'userGroupName')
on conflict (id) do nothing;

INSERT INTO public.i18n (id, name, key, i18n_type_id)
VALUES ('00000000-0000-0001-0006-000000000002', 'user_group[domain_admin]', null, 'userGroupDescription')
on conflict (id) do nothing;

INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('00000000-0000-0001-0006-000000000001', 'en', 'Domain admin', 2)
on conflict (i18n_id, locale) do nothing;
INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter)
VALUES ('00000000-0000-0001-0006-000000000002', 'en', '', 2)
on conflict (i18n_id, locale) do nothing;

INSERT INTO user_group (id, domain_id, business_account_id, user_group_type_id, name_i18n_id, description_i18n_id)
VALUES ('00000000-0000-0000-0006-000000000001', null, null,
        'systemScopeDomainManage', '00000000-0000-0001-0006-000000000001',
        '00000000-0000-0001-0006-000000000002')
on conflict (id) do nothing;



create table if not exists permission_grant_global
(
    id                 uuid not null
        constraint permission_grant_global_pk
            primary key,
    permission_id      uuid not null
        constraint permission_grant_global_permission_id_fk
            references permission
            on update cascade on delete cascade,
    user_group_id      uuid not null
        constraint permission_grant_global_user_group_id_fk
            references user_group
            on update cascade,
    granted_by_user_id uuid not null
        constraint permission_grant_global_user_id_fk
            references "user"
            on update cascade,
    granted_at         timestamp default CURRENT_TIMESTAMP
);

create unique index if not exists permission_grant_global_uniq1
    on permission_grant_global (permission_id, user_group_id);

INSERT INTO permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000001',
        '00000000-0000-0000-0004-000000000002', '00000000-0000-0000-0006-000000000001',
        '00000000-0000-0000-0000-000000000000', '2024-03-05 14:01:12.000000')
on conflict (id) do nothing;
INSERT INTO permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000002',
        '00000000-0000-0000-0004-000000000003', '00000000-0000-0000-0006-000000000001',
        '00000000-0000-0000-0000-000000000000', '2024-03-05 14:01:12.000000')
on conflict (id) do nothing;
INSERT INTO permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000003',
        '00000000-0000-0000-0004-000000000004', '00000000-0000-0000-0006-000000000001',
        '00000000-0000-0000-0000-000000000000', '2024-03-05 14:01:12.000000')
on conflict (id) do nothing;
INSERT INTO permission_grant_global
    (id, permission_id, user_group_id, granted_by_user_id, granted_at)
VALUES ('00000000-0000-0000-0007-000000000004',
        '00000000-0000-0000-0004-000000000005', '00000000-0000-0000-0006-000000000001',
        '00000000-0000-0000-0000-000000000000', '2024-03-05 14:01:12.000000')
on conflict (id) do nothing;




