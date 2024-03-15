alter table if exists twin_attachment
    drop column if exists twin_comment_id;

alter table twin_attachment
    add if not exists twin_comment_id uuid;

alter table twin_attachment
    add constraint twin_attachment_twin_comment_id_fk
        foreign key (twin_comment_id) references twin_comment
            on delete cascade;
