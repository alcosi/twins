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
    business_account_id uuid not null
        constraint user_group_map_business_account_id_fk
            references business_account
            on update cascade on delete cascade,
    user_id             uuid not null
        constraint user_group_map_user_id_fk
            references "user"
            on update cascade on delete cascade,
    involves_counter int not null default 0,
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



create or replace function user_group_map_bi_fn()
    returns trigger
    language plpgsql
as
$$
declare
    v_group record;
begin
    -- 1. Запрет явного задания user_group_type_id
    if new.user_group_type_id is not null then
        raise exception 'user_group_type_id must not be set explicitly';
    end if;

    -- 2. Получаем группу
    select *
    into v_group
    from user_group
    where id = new.user_group_id;

    if not found then
        raise exception 'user_group not found for id=%', new.user_group_id;
    end if;

    -- 3. Проставляем тип из группы
    new.user_group_type_id := v_group.user_group_type_id;

    -- 4. Логика по типу
    case v_group.user_group_type_id

        when 'systemScopeDomainManage' then
            if new.domain_id is null then
                raise exception 'domain_id must be provided for systemScopeDomainManage';
            end if;

            if new.business_account_id is not null then
                raise exception 'business_account_id must be null for systemScopeDomainManage';
            end if;


        when 'businessAccountScopeBusinessAccountManage' then
            if new.domain_id is not null then
                raise exception 'domain_id must be null for businessAccountScopeBusinessAccountManage';
            end if;

            if new.business_account_id is not null then
                raise exception 'business_account_id must be null for businessAccountScopeBusinessAccountManage';
            end if;

            new.domain_id := v_group.domain_id;


        when 'domainScopeDomainManage' then
            if new.domain_id is not null then
                raise exception 'domain_id must be null for domainScopeDomainManage';
            end if;

            if new.business_account_id is not null then
                raise exception 'business_account_id must be null for domainScopeDomainManage';
            end if;

            new.domain_id := v_group.domain_id;


        when 'domainScopeBusinessAccountManage' then
            if new.domain_id is not null then
                raise exception 'domain_id must be null for domainScopeBusinessAccountManage';
            end if;

            if new.business_account_id is null then
                raise exception 'business_account_id must be provided for domainScopeBusinessAccountManage';
            end if;

            new.domain_id := v_group.domain_id;


        when 'domainAndBusinessAccountScopeBusinessAccountManage' then
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
