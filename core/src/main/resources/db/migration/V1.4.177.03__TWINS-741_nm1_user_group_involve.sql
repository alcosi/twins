-- 1. Rename table first to ensure all further operations use the new name
alter table if exists user_group_act_as_user_involve
    rename to user_group_involve_act_as_user;

-- 2. Rename column to reflect new naming convention
alter table user_group_involve_act_as_user
    rename column involve_in_user_group_id to user_group_id;

-- 3. Recreate primary key constraint with new name
-- (PostgreSQL automatically creates an index for primary key)
alter table user_group_involve_act_as_user
    drop constraint if exists user_group_act_as_user_involve_pkey;

alter table user_group_involve_act_as_user
    add constraint user_group_involve_act_as_user_pkey
        primary key (id);

-- 4. Recreate unique index with updated naming
drop index if exists user_group_act_as_user_involve_user_id_domain_id_uindex;

create unique index user_group_involve_act_as_user_uindex1
    on user_group_involve_act_as_user(domain_id, machine_user_id, user_group_id);

-- 5. Recreate non-unique index for added_by_user_id
drop index if exists idx_user_group_act_as_user_involve_added_by_user_id;

create index idx_user_group_involve_act_as_user_added_by_user_id
    on user_group_involve_act_as_user(added_by_user_id);

-- 6. Recreate foreign key to domain table
alter table user_group_involve_act_as_user
    drop constraint if exists user_group_act_as_user_involve_domain_id_fk;

alter table user_group_involve_act_as_user
    add constraint user_group_involve_act_as_user_domain_id_fk
        foreign key (domain_id) references domain(id);

-- 7. Recreate foreign key to user table (machine_user_id)
alter table user_group_involve_act_as_user
    drop constraint if exists user_group_act_as_user_involve_user_id_fk;

alter table user_group_involve_act_as_user
    add constraint user_group_involve_act_as_user_user_id_fk
        foreign key (machine_user_id) references "user"(id);

-- 8. Recreate foreign key to user_group table
alter table user_group_involve_act_as_user
    drop constraint if exists user_group_act_as_user_involve_user_group_id_fk;

alter table user_group_involve_act_as_user
    add constraint user_group_involve_act_as_user_user_group_id_fk
        foreign key (user_group_id) references user_group(id);

-- 9. Recreate foreign key to user table (added_by_user_id)
alter table user_group_involve_act_as_user
    drop constraint if exists user_group_act_as_user_involve_added_user_id_fk;

alter table user_group_involve_act_as_user
    add constraint user_group_involve_act_as_user_added_user_id_fk
        foreign key (added_by_user_id) references "user"(id);