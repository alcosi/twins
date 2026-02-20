create or replace function twin_class_before_update_wrapper() returns trigger
    language plpgsql
as
$$
begin

    if new.twin_class_owner_type_id is distinct from old.twin_class_owner_type_id then
        perform twin_class_prevent_owner_type_change(old);
    end if;

    if new.extends_hierarchy_tree is distinct from old.extends_hierarchy_tree and old.extends_hierarchy_tree is not null then
        new := twin_class_update_extends_hierarchy_tree(old, new);
    end if;

    return new;
end;
$$;


create or replace function twin_class_prevent_owner_type_change(old twin_class)
    returns void volatile
    language plpgsql
as
$$
declare
    v_exists boolean;
begin
        -- Check if this class is used in propagation table
        select exists (
            select 1
            from user_group_involve_assignee p
            where p.propagation_by_twin_class_id = old.id
        )
        into v_exists;

        if v_exists then
            raise exception
                'Cannot change twin_class_owner_type_id because the class is used in user_group_involve_assignee';
        end if;
end;
$$;
