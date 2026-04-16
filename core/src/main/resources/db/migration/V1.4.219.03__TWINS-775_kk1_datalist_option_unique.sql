-- Add unique constraint to prevent duplicate data list options by externalId
-- First, clean up existing duplicates
-- IMPORTANT: Before deleting duplicates, consolidate references in twin_field_data_list

-- Create temporary table to track duplicate consolidation
create temporary table if not exists datalist_option_duplicates (
    primary_option_id uuid,
    duplicate_option_id uuid
);

-- Find duplicates for non-custom options (custom = false, business_account_id is null)
insert into datalist_option_duplicates (primary_option_id, duplicate_option_id)
select
    first_value(id) over (partition by data_list_id, external_id order by created_at asc) as primary_option_id,
    id as duplicate_option_id
from data_list_option
where custom = false
  and external_id is not null
  and business_account_id is null;

-- Find duplicates for custom options (custom = true, business_account_id is not null)
insert into datalist_option_duplicates (primary_option_id, duplicate_option_id)
select
    first_value(id) over (partition by data_list_id, business_account_id, external_id order by created_at asc) as primary_option_id,
    id as duplicate_option_id
from data_list_option
where custom = true
  and business_account_id is not null
  and external_id is not null;

-- Remove self-references (where primary = duplicate)
delete from datalist_option_duplicates
where primary_option_id = duplicate_option_id;

-- Update twin_field_data_list to reference primary options instead of duplicates
update twin_field_data_list
set data_list_option_id = d.primary_option_id
from datalist_option_duplicates d
where data_list_option_id = d.duplicate_option_id;

-- Now delete duplicates (custom = false)
delete from data_list_option
where id in (
    select duplicate_option_id
    from datalist_option_duplicates d
    join data_list_option opt on d.duplicate_option_id = opt.id
    where opt.custom = false
);

-- Delete duplicates (custom = true)
delete from data_list_option
where id in (
    select duplicate_option_id
    from datalist_option_duplicates d
    join data_list_option opt on d.duplicate_option_id = opt.id
    where opt.custom = true
);

-- Drop temporary table
drop table datalist_option_duplicates;

-- Create full unique index (no WHERE clause) - PostgreSQL can use it for ON CONFLICT
create unique index if not exists datalist_opt_dl_id_ext_id_ba_acc_uindex
    on data_list_option (data_list_id, business_account_id, external_id);
