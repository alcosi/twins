create table if not exists user_group_map
(
    id                  uuid not null
        primary key,
    user_group_id       uuid not null
        constraint user_group_map_user_group_id_fk
            references user_group
            on update cascade on delete cascade,
    user_group_type_id  varchar not null
        constraint user_group_map_user_group_type_id_fk
            references user_group_type
            on update cascade,
    domain_id        uuid not null
        constraint user_group_map_domain_id_fk
            references domain
            on update cascade on delete cascade,
    business_account_id uuid
        constraint user_group_map_business_account_id_fk
            references business_account
            on update cascade on delete cascade,
    user_id             uuid not null
        constraint user_group_map_user_id_fk
            references "user"
            on update cascade on delete cascade,
    involves_counter int not null default 0,
    added_manually boolean not null default true,
    added_at            timestamp default CURRENT_TIMESTAMP,
    added_by_user_id    uuid
        constraint user_group_map_added_user_id_fk
            references "user"
            on update cascade
);

drop index if exists user_group_map_user_id_business_account_id_index;
create unique index user_group_map_user_id_business_account_id_index
    on user_group_map (user_id, business_account_id, user_group_id);

drop index if exists idx_user_group_map_added_by_user_id;
create index idx_user_group_map_added_by_user_id
    on user_group_map (added_by_user_id);



create or replace function user_group_map_validate_domain_and_business_account(NEW user_group_map)
    returns user_group_map
    volatile
    language plpgsql
as
$$
declare
    v_group record;
begin
    -- Prevent explicit setting of user_group_type_id
    if new.user_group_type_id is not null then
        raise exception 'user_group_type_id must not be set explicitly';
    end if;

    -- Load user_group by id
    select *
    into v_group
    from user_group
    where id = new.user_group_id;

    if not found then
        raise exception 'user_group not found for id=%', new.user_group_id;
    end if;

    -- Assign type from user_group
    new.user_group_type_id := v_group.user_group_type_id;

    -- Apply validation and autofill rules depending on group type
    case v_group.user_group_type_id

        when 'systemScopeDomainManage' then
            -- domain_id must be provided, business_account_id must be null
            if new.domain_id is null then
                raise exception 'domain_id must be provided for systemScopeDomainManage';
            end if;

            if new.business_account_id is not null then
                raise exception 'business_account_id must be null for systemScopeDomainManage';
            end if;


        when 'businessAccountScopeBusinessAccountManage' then
            -- domain_id and business_account_id must be null, domain_id taken from group
            if new.domain_id is not null then
                raise exception 'domain_id must be null for businessAccountScopeBusinessAccountManage';
            end if;

            if new.business_account_id is not null then
                raise exception 'business_account_id must be null for businessAccountScopeBusinessAccountManage';
            end if;

            new.domain_id := v_group.domain_id;


        when 'domainScopeDomainManage' then
            -- domain_id and business_account_id must be null, domain_id taken from group
            if new.domain_id is not null then
                raise exception 'domain_id must be null for domainScopeDomainManage';
            end if;

            if new.business_account_id is not null then
                raise exception 'business_account_id must be null for domainScopeDomainManage';
            end if;

            new.domain_id := v_group.domain_id;


        when 'domainScopeBusinessAccountManage' then
            -- domain_id must be null, business_account_id must be provided, domain_id taken from group
            if new.domain_id is not null then
                raise exception 'domain_id must be null for domainScopeBusinessAccountManage';
            end if;

            if new.business_account_id is null then
                raise exception 'business_account_id must be provided for domainScopeBusinessAccountManage';
            end if;

            new.domain_id := v_group.domain_id;


        when 'domainAndBusinessAccountScopeBusinessAccountManage' then
            -- domain_id and business_account_id must be null, both taken from group
            if new.domain_id is not null then
                raise exception 'domain_id must be null for domainAndBusinessAccountScopeBusinessAccountManage';
            end if;

            if new.business_account_id is not null then
                raise exception 'business_account_id must be null for domainAndBusinessAccountScopeBusinessAccountManage';
            end if;

            new.domain_id := v_group.domain_id;
            new.business_account_id := v_group.business_account_id;


        else
            raise exception 'Unsupported user_group_type_id=%',
                v_group.user_group_type_id;

        end case;

    return new;
end;
$$;

INSERT INTO user_group_map (id, user_group_id, user_group_type_id, domain_id, business_account_id, user_id, involves_counter, added_manually, added_at, added_by_user_id)
SELECT
    t2.id,
    t2.user_group_id,
    ug.user_group_type_id,
    ug.domain_id,
    t2.business_account_id,
    t2.user_id,
    0,
    true,
    t2.added_at,
    t2.added_by_user_id
FROM user_group_map_type2 t2
JOIN user_group ug ON t2.user_group_id = ug.id
ON CONFLICT DO NOTHING;

INSERT INTO user_group_map (id, user_group_id, user_group_type_id, domain_id, business_account_id, user_id, involves_counter, added_manually, added_at, added_by_user_id)
SELECT
    t3.id,
    t3.user_group_id,
    ug.user_group_type_id,
    t3.domain_id,
    ug.business_account_id,
    t3.user_id,
    0,
    true,
    t3.added_at,
    t3.added_by_user_id
FROM user_group_map_type3 t3
JOIN user_group ug ON t3.user_group_id = ug.id
ON CONFLICT DO NOTHING;

INSERT INTO user_group_map (id, user_group_id, user_group_type_id, domain_id, business_account_id, user_id, involves_counter, added_manually, added_at, added_by_user_id)
SELECT
    t1.id,
    t1.user_group_id,
    ug.user_group_type_id,
    ug.domain_id,
    ug.business_account_id,
    t1.user_id,
    0,
    true,
    t1.added_at,
    t1.added_by_user_id
FROM user_group_map_type1 t1
JOIN user_group ug ON t1.user_group_id = ug.id
ON CONFLICT DO NOTHING;

--todo count involves?
