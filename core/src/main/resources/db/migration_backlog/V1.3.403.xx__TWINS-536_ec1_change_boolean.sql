--  maybe need to set all null values to false?
update twin_field_boolean
set value = false
where value is null;

alter table twin_field_boolean
    alter column value set not null;


update draft_twin_field_boolean
set value = false
where value is null;

alter table draft_twin_field_boolean
    alter column value set not null;
