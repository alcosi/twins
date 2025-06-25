alter table face_tw001
    drop constraint if exists face_tw001_twin_pointer_id_fk;

alter table face_tw001
    add constraint face_tw001_twin_pointer_id_fk
        foreign key (target_twin_pointer_id) references twin_pointer
            on update cascade on delete restrict;

alter table face_tw002
    drop constraint if exists face_tw002_twin_pointer_id_fk;

alter table face_tw002
    add constraint face_tw002_twin_pointer_id_fk
        foreign key (target_twin_pointer_id) references twin_pointer
            on update cascade on delete restrict;

alter table face_tw004
    drop constraint if exists face_tw004_twin_pointer_id_fk;

alter table face_tw004
    add constraint face_tw004_twin_pointer_id_fk
        foreign key (target_twin_pointer_id) references twin_pointer
            on update cascade on delete restrict;

alter table face_tw005
    drop constraint if exists face_tw005_twin_pointer_id_fk;

alter table face_tw005
    add constraint face_tw005_twin_pointer_id_fk
        foreign key (target_twin_pointer_id) references twin_pointer
            on update cascade on delete restrict;

alter table face_tc001
    drop constraint if exists face_tc001_twin_pointer_id_fk;

alter table face_tc001
    add constraint face_tc001_twin_pointer_id_fk
        foreign key (head_twin_pointer_id) references twin_pointer
            on update cascade on delete restrict;