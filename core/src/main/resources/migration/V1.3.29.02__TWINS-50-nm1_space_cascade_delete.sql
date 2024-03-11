alter table space
    drop constraint if exists space_twin_id_fk;

alter table space
    add constraint space_twin_id_fk
        foreign key (twin_id) references twin
            on update cascade on delete cascade;

alter table space_role_user
    drop constraint if exists  space_role_user_twin_id_fk;

alter table space_role_user
    add constraint space_role_user_twin_id_fk
        foreign key (twin_id) references twin
            on update cascade on delete cascade;

alter table space_role_user_group
    drop constraint if exists space_role_user_group_twin_id_fk;

alter table space_role_user_group
    add constraint space_role_user_group_twin_id_fk
        foreign key (twin_id) references twin
            on update cascade on delete cascade;