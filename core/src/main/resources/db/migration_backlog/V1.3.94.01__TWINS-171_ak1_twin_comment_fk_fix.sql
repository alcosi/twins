alter table public.twin_comment
    drop constraint twin_comment_twin_id_fk;

alter table public.twin_comment
    add constraint twin_comment_twin_id_fk
        foreign key (twin_id) references public.twin
            on update cascade on delete cascade;
