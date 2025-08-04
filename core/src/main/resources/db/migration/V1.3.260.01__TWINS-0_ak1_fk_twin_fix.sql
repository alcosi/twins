alter table public.twin
    drop constraint twin_twin_class_id_fk;

alter table public.twin
    add constraint twin_twin_class_id_fk
        foreign key (twin_class_id) references public.twin_class
            on update cascade on delete cascade;

