alter table if exists twin_attachment
    add if not exists twin_comment_id uuid;

alter table twin_attachment
    drop constraint if exists twin_attachment_twin_comment_id_fk;

alter table if exists twin_attachment
    add constraint twin_attachment_twin_comment_id_fk
        foreign key (twin_comment_id) references twin_comment
            on delete cascade;