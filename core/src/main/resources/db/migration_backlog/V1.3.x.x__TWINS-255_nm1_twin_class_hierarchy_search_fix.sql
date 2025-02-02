drop function if exists twin_class_head_hierarchy_any_of(uuid, text[]);
create function twin_class_head_hierarchy_any_of(domainId uuid, lquery_array text[]) RETURNS SETOF twin_class
    immutable
    strict
    language plpgsql
as
$$
BEGIN
    RETURN QUERY
        select * from twin_class where domain_id = domainId and head_hierarchy_tree ~ any (lquery_array::lquery[]);
END;
$$;

drop function if exists twin_class_extends_hierarchy_any_of(uuid, text[]);
create function twin_class_extends_hierarchy_any_of(domainId uuid, lquery_array text[]) RETURNS SETOF twin_class
    immutable
    strict
    language plpgsql
as
$$
BEGIN
    RETURN QUERY
        select *
        from twin_class
        where domain_id = domainId and twin_class.extends_hierarchy_tree ~ any (lquery_array::lquery[]);
END;
$$;