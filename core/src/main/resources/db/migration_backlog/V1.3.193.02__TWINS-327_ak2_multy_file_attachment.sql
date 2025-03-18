-- new table creation;
CREATE TABLE if not exists twin_attachment_modification_links (
                                               twin_attachment_id UUID NOT NULL,
                                               mod_key VARCHAR(255) NOT NULL,
                                               mod_link TEXT,
                                               CONSTRAINT FK_twin_attachment_storage_links_twin_attachment_id
                                                   FOREIGN KEY (twin_attachment_id)
                                                       REFERENCES twin_attachment(id)
                                                       ON DELETE CASCADE,
                                               CONSTRAINT PK_twin_attachment_storage_links
                                                   PRIMARY KEY (twin_attachment_id, mod_key)
);
drop index if exists IDX_twin_attachment_mod_links_twin_attachment_id;
CREATE INDEX IDX_twin_attachment_mod_links_twin_attachment_id ON twin_attachment_modification_links(twin_attachment_id);

CREATE TABLE if not exists draft_twin_attachment_modification_links (
                                                     draft_twin_attachment_id UUID NOT NULL,
                                                     mod_key VARCHAR(255) NOT NULL,
                                                     mod_link TEXT,
                                                     CONSTRAINT FK_draft_twin_attachment_storage_links_draft_twin_attachment_id
                                                         FOREIGN KEY (draft_twin_attachment_id)
                                                             REFERENCES draft_twin_attachment(id)
                                                             ON DELETE CASCADE,
                                                     CONSTRAINT PK_draft_twin_attachment_storage_links
                                                         PRIMARY KEY (draft_twin_attachment_id, mod_key)
);


drop index if exists IDX_draft_twin_attach_mod_links_draft_twin_attachment_id;
CREATE INDEX IDX_draft_twin_attach_mod_links_draft_twin_attachment_id
    ON draft_twin_attachment_modification_links(draft_twin_attachment_id);
