alter table public.twin_class
    drop constraint twin_class_head_twin_class_id_fk;

alter table public.twin_class
    add constraint twin_class_head_twin_class_id_fk
        foreign key (head_twin_class_id) references public.twin_class
            on update cascade on delete cascade;

alter table public.twin_class
    drop constraint twin_class_extends_twin_class_id_fk;

alter table public.twin_class
    add constraint twin_class_extends_twin_class_id_fk
        foreign key (extends_twin_class_id) references public.twin_class
            on update cascade on delete cascade;

