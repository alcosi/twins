create or replace function check_ltree_any_is_in(ltree_root_val ltree,search_values text[]) returns bool
    language sql
    immutable parallel safe
    returns null on null input
as
$$
select ltree_root_val ~ any (search_values::lquery[])
$$;


create or replace function check_ltree_all_is_in(ltree_root_val ltree,search_values text[]) returns bool
    language sql
    immutable parallel safe
    returns null on null input
as
$$
select ltree_root_val ~ all (search_values::lquery[])
$$;