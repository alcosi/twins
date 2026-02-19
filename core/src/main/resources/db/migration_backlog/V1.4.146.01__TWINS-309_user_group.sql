create or replace function user_group_validate_domain_and_account(
    p_type varchar,
    p_domain_id uuid,
    p_business_account_id uuid
)
    returns void
    language plpgsql
as
$$
begin
    -- Validate domain_id and business_account_id depending on group type
    case p_type

        when 'systemScopeDomainManage' then
            if p_domain_id is not null then
                raise exception 'domain_id must be null for systemScopeDomainManage';
            end if;
            if p_business_account_id is not null then
                raise exception 'business_account_id must be null for systemScopeDomainManage';
            end if;

        when 'businessAccountScopeBusinessAccountManage' then
            if p_domain_id is not null then
                raise exception 'domain_id must be null for businessAccountScopeBusinessAccountManage';
            end if;
            if p_business_account_id is null then
                raise exception 'business_account_id must be provided for businessAccountScopeBusinessAccountManage';
            end if;

        when 'domainScopeBusinessAccountManage' then
            if p_domain_id is null then
                raise exception 'domain_id must be provided for domainScopeBusinessAccountManage';
            end if;
            if p_business_account_id is not null then
                raise exception 'business_account_id must be null for domainScopeBusinessAccountManage';
            end if;

        when 'domainAndBusinessAccountScopeBusinessAccountManage' then
            if p_domain_id is null then
                raise exception 'domain_id must be provided for domainAndBusinessAccountScopeBusinessAccountManage';
            end if;
            if p_business_account_id is null then
                raise exception 'business_account_id must be provided for domainAndBusinessAccountScopeBusinessAccountManage';
            end if;

        else
            raise exception 'Unsupported user_group_type_id=%', p_type;

        end case;
end;
$$;


create or replace function user_group_bi_fn()
    returns trigger
    language plpgsql
as
$$
begin
    -- Use shared validation
    perform user_group_validate_domain_and_account(new.user_group_type_id, new.domain_id, new.business_account_id);
    return new;
end;
$$;

create or replace function user_group_bu_fn()
    returns trigger
    language plpgsql
as
$$
begin
    -- Prevent changing user_group_type_id
    if new.user_group_type_id is distinct from old.user_group_type_id then
        raise exception 'user_group_type_id cannot be changed';
    end if;

    -- Use shared validation
    perform user_group_validate_domain_and_account(new.user_group_type_id, new.domain_id, new.business_account_id);
    return new;
end;
$$;

create or replace function user_group_au_fn()
    returns trigger
    language plpgsql
as
$$
begin
    -- Update domain_id in user_group_map if domain_id changed
    if new.domain_id is distinct from old.domain_id then
        update user_group_map
        set domain_id = new.domain_id
        where user_group_id = new.id;
    end if;

    -- Update business_account_id in user_group_map if business_account_id changed
    if new.business_account_id is distinct from old.business_account_id then
        update user_group_map
        set business_account_id = new.business_account_id
        where user_group_id = new.id;
    end if;

    return null; -- AFTER trigger does not need to return NEW
end;
$$;