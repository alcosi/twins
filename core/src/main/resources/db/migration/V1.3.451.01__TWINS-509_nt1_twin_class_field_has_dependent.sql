alter table twin_class_field
    add column if not exists dependent_field boolean default false not null;
alter table twin_class_field
    add column if not exists has_dependent_fields boolean default false not null;

create index if not exists twin_class_field_dependent_field_index
    on twin_class_field (dependent_field);

create index if not exists twin_class_field_has_dependent_fields_index
    on twin_class_field (has_dependent_fields);

CREATE OR REPLACE FUNCTION twin_class_field_is_dependent_field_check(field_id uuid) returns void
    LANGUAGE plpgsql
AS $$
BEGIN
    IF field_id IS NOT NULL THEN
UPDATE twin_class_field
SET dependent_field = (
    SELECT EXISTS (
        SELECT 1
        FROM twin_class_field_rule rule
        WHERE rule.twin_class_field_id = field_id    )
)
WHERE id = field_id;
END IF;
END;
$$;

----- conditions triggers
CREATE OR REPLACE FUNCTION twin_class_field_has_dependent_fields_check(field_id uuid) returns void
    LANGUAGE plpgsql
AS $$
BEGIN
    IF field_id IS NOT NULL THEN
UPDATE twin_class_field
SET has_dependent_fields = (
    SELECT EXISTS (
        SELECT 1
        FROM twin_class_field_condition condition
        WHERE condition.base_twin_class_field_id = field_id    )
)
WHERE id = field_id;
END IF;
END;
$$;

create or replace function twin_class_field_condition_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
begin
    perform twin_class_field_has_dependent_fields_check(old.base_twin_class_field_id);
return old;
end;
$$;

drop trigger if exists twin_class_field_condition_after_delete_wrapper_trigger on twin_class_field_condition;

create trigger twin_class_field_condition_after_delete_wrapper_trigger
    after delete on twin_class_field_condition
    for each row
execute procedure twin_class_field_condition_after_delete_wrapper();


create or replace function twin_class_field_condition_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
begin
    perform twin_class_field_has_dependent_fields_check(new.base_twin_class_field_id);
return new;
end;
$$;

drop trigger if exists twin_class_field_condition_after_insert_wrapper_trigger on twin_class_field_condition;
create trigger twin_class_field_condition_after_insert_wrapper_trigger
    after insert on twin_class_field_condition
    for each row
execute procedure twin_class_field_condition_after_insert_wrapper();

create or replace function twin_class_field_condition_after_update_wrapper() returns trigger
    language plpgsql
as
$$
begin

    if new.base_twin_class_field_id is distinct from old.base_twin_class_field_id then
        perform twin_class_field_has_dependent_fields_check(old.base_twin_class_field_id);
        perform twin_class_field_has_dependent_fields_check(new.base_twin_class_field_id);
    end if;

return new;
end;
$$;

drop trigger if exists twin_class_field_condition_after_update_wrapper_trigger on twin_class_field_condition;

create trigger twin_class_field_condition_after_update_wrapper_trigger
    after update on twin_class_field_condition
    for each row
execute procedure twin_class_field_condition_after_update_wrapper();

----- rules triggers

create or replace function twin_class_field_rule_after_delete_wrapper() returns trigger
    language plpgsql
as
$$
begin
    perform twin_class_field_is_dependent_field_check(old.twin_class_field_id);
return old;
end;
$$;

drop trigger if exists twin_class_field_rule_after_delete_wrapper_trigger on twin_class_field_rule;
create trigger twin_class_field_rule_after_delete_wrapper_trigger
    after delete on twin_class_field_rule
    for each row
execute procedure twin_class_field_rule_after_delete_wrapper();

create or replace function twin_class_field_rule_after_insert_wrapper() returns trigger
    language plpgsql
as
$$
begin
    perform twin_class_field_is_dependent_field_check(new.twin_class_field_id);
return new;
end;
$$;

drop trigger if exists twin_class_field_rule_after_insert_wrapper_trigger on twin_class_field_rule;
create trigger twin_class_field_rule_after_insert_wrapper_trigger
    after insert on twin_class_field_rule
    for each row
execute procedure twin_class_field_rule_after_insert_wrapper();


create or replace function twin_class_field_rule_after_update_wrapper() returns trigger
    language plpgsql
as
$$
begin
    -- Update dependent_field if extends_twin_class_field_id changed
    if new.twin_class_field_id is distinct from old.twin_class_field_id then
        perform twin_class_field_is_dependent_field_check(old.twin_class_field_id);
        perform twin_class_field_is_dependent_field_check(new.twin_class_field_id);
    end if;

    return new;
end;
$$;

drop trigger if exists twin_class_field_rule_after_update_wrapper_trigger on twin_class_field_rule;

create trigger twin_class_field_rule_after_update_wrapper_trigger
    after update on twin_class_field_rule
    for each row
execute procedure twin_class_field_rule_after_update_wrapper();


