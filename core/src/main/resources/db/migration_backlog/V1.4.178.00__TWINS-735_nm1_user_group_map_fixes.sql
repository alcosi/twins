drop index if exists idx_ugm_scope_without_ba;

drop index if exists idx_ugm_scope_with_ba;

create unique index idx_ugm_scope
    on user_group_map (
                       user_group_id,
                       domain_id,
                       coalesce(business_account_id, '00000000-0000-0000-0000-000000000000'),
                       user_id
        );


create or replace function user_group_map_validate_domain_and_business_account(NEW user_group_map)
    returns user_group_map
    volatile
    language plpgsql
as
$$
declare
    v_group record;
begin
    -- Load user_group by id
    select *
    into v_group
    from user_group
    where id = new.user_group_id;

    if not found then
        raise exception 'user_group not found for id=%', new.user_group_id;
    end if;


    -- Prevent explicit setting of user_group_type_id
    if new.user_group_type_id is not null and new.user_group_type_id is distinct from v_group.user_group_type_id then
        raise exception 'user_group_type_id must not be set explicitly';
    end if;

    -- Assign type from user_group
    new.user_group_type_id := v_group.user_group_type_id;

    -- Apply validation and autofill rules depending on group type
    case v_group.user_group_type_id

        when 'systemScopeDomainManage' then
            -- domain_id in map represents user context, not group property.
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
            if new.domain_id is not null and new.domain_id is distinct from v_group.domain_id then
                raise exception 'domain_id must be null for domainScopeDomainManage';
            end if;

            if new.business_account_id is not null then
                raise exception 'business_account_id must be null for domainScopeDomainManage';
            end if;

            new.domain_id := v_group.domain_id;


        when 'domainScopeBusinessAccountManage' then
            -- domain_id must be null, business_account_id must be provided, domain_id taken from group
            if new.domain_id is not null and new.domain_id is distinct from v_group.domain_id then
                raise exception 'domain_id must be null for domainScopeBusinessAccountManage';
            end if;

            if new.business_account_id is null then
                raise exception 'business_account_id must be provided for domainScopeBusinessAccountManage';
            end if;

            new.domain_id := v_group.domain_id;


        when 'domainAndBusinessAccountScopeBusinessAccountManage' then
            -- domain_id and business_account_id must be null, both taken from group
            if new.domain_id is not null and new.domain_id is distinct from v_group.domain_id  then
                raise exception 'domain_id must be null for domainAndBusinessAccountScopeBusinessAccountManage';
            end if;

            if new.business_account_id is not null  and new.business_account_id is distinct from v_group.business_account_id  then
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