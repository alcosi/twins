alter table user_group_act_as_user_involve
    rename column involve_in_user_group_id to user_group_id;

drop index if exists user_group_act_as_user_involve_pkey;
create unique index user_group_involve_act_as_user_pkey
    on user_group_act_as_user_involve(user_group_id, user_id, domain_id); -- вставить реальные PK поля

drop index if exists user_group_act_as_user_involve_user_id_domain_id_uindex;
create unique index user_group_involve_act_as_user_uindex1
    on user_group_act_as_user_involve(user_id, domain_id);

drop index if exists idx_user_group_act_as_user_involve_added_by_user_id;
create index idx_user_group_involve_act_as_user_added_by_user_id
    on user_group_act_as_user_involve(added_by_user_id);

alter table user_group_act_as_user_involve
    drop constraint if exists user_group_act_as_user_involve_pkey;

alter table user_group_act_as_user_involve
    add constraint user_group_involve_act_as_user_pkey
        primary key (id);

alter table user_group_act_as_user_involve
    drop constraint if exists user_group_act_as_user_involve_domain_id_fk;
alter table user_group_act_as_user_involve
    add constraint user_group_involve_act_as_user_domain_id_fk
        foreign key (domain_id) references domain(id);

alter table user_group_act_as_user_involve
    drop constraint if exists user_group_act_as_user_involve_user_id_fk;
alter table user_group_act_as_user_involve
    add constraint user_group_involve_act_as_user_user_id_fk
        foreign key (user_id) references "user"(id);

alter table user_group_act_as_user_involve
    drop constraint if exists user_group_act_as_user_involve_user_group_id_fk;
alter table user_group_act_as_user_involve
    add constraint user_group_involve_act_as_user_user_group_id_fk
        foreign key (user_group_id) references user_group(id);

alter table user_group_act_as_user_involve
    drop constraint if exists user_group_act_as_user_involve_added_user_id_fk;
alter table user_group_act_as_user_involve
    add constraint user_group_involve_act_as_user_added_user_id_fk
        foreign key (added_by_user_id) references "user"(id);



alter table if exists user_group_act_as_user_involve
    rename to user_group_involve_act_as_user;