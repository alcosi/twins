-- Add unique constraint to prevent duplicate data list options by externalId
-- First, clean up existing duplicates

delete from data_list_option
where id in (
    select id
    from (
        select id,
               row_number() over (
                   partition by data_list_id, external_id
                   order by created_at asc
               ) as rn
        from data_list_option
        where custom = false
          and external_id is not null
    ) t
    where rn > 1
);

-- Create full unique index (no WHERE clause) - PostgreSQL can use it for ON CONFLICT
create unique index if not exists datalist_opt_dl_id_ext_id_ba_acc_uindex
    on public.data_list_option (data_list_id, external_id, business_account_id);
