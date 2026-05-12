begin;

with ranked as (
    select
        id,
        row_number() over (
            partition by twin_id, twin_class_field_id, data_list_option_id
            order by id
        ) as rn
    from twin_field_data_list
)
delete from twin_field_data_list t
    using ranked r
where t.id = r.id
  and r.rn > 1;

create unique index if not exists twin_field_data_list_twin_tcf_dlo_uindex
    on twin_field_data_list (twin_id, twin_class_field_id, data_list_option_id);

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'twin_field_data_list_twin_tcf_dlo_uk'
          and conrelid = 'twin_field_data_list'::regclass
    ) then
alter table twin_field_data_list
    add constraint twin_field_data_list_twin_tcf_dlo_uk
        unique using index twin_field_data_list_twin_tcf_dlo_uindex;
end if;
end
$$;

commit;