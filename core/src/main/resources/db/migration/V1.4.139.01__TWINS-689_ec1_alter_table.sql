delete
from twin_field_timestamp
where value is null;

alter table if exists twin_field_timestamp
    alter column value set not null;
