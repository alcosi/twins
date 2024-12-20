alter table public.twin_class add column if not exists create_permission_id uuid;
alter table public.twin_class add column if not exists edit_permission_id uuid;
alter table public.twin_class add column if not exists delete_permission_id uuid;

CREATE TABLE twin_class_temp (
                                 id UUID PRIMARY KEY,
                                 domain_id UUID,
                                 key TEXT,
                                 create_permission_id UUID,
                                 edit_permission_id UUID,
                                 delete_permission_id UUID,
                                 view_permission_id UUID,
                                 permission_schema_space BOOLEAN,
                                 twinflow_schema_space BOOLEAN,
                                 twin_class_schema_space BOOLEAN,
                                 alias_space BOOLEAN,
                                 abstract BOOLEAN,
                                 name_i18n_id UUID,
                                 description_i18n_id UUID,
                                 created_by_user_id UUID,
                                 created_at TIMESTAMP,
                                 logo TEXT,
                                 head_twin_class_id UUID,
                                 extends_twin_class_id UUID,
                                 head_hierarchy_tree ltree,
                                 extends_hierarchy_tree ltree,
                                 domain_alias_counter INTEGER,
                                 marker_data_list_id UUID,
                                 tag_data_list_id UUID,
                                 twin_class_owner_type_id UUID,
                                 head_hunter_featurer_id INTEGER,
                                 head_hunter_featurer_params hstore
);

INSERT INTO twin_class_temp (
       id, domain_id, key, create_permission_id, edit_permission_id, delete_permission_id, view_permission_id, permission_schema_space, twinflow_schema_space, twin_class_schema_space, alias_space, abstract, name_i18n_id, description_i18n_id, created_by_user_id, created_at, logo, head_twin_class_id, extends_twin_class_id, head_hierarchy_tree, extends_hierarchy_tree, domain_alias_counter, marker_data_list_id, tag_data_list_id, twin_class_owner_type_id, head_hunter_featurer_id, head_hunter_featurer_params
)
SELECT id, domain_id, key, create_permission_id, edit_permission_id, delete_permission_id, view_permission_id, permission_schema_space, twinflow_schema_space, twin_class_schema_space, alias_space, abstract, name_i18n_id, description_i18n_id, created_by_user_id, created_at, logo, head_twin_class_id, extends_twin_class_id, head_hierarchy_tree, extends_hierarchy_tree, domain_alias_counter, marker_data_list_id, tag_data_list_id, twin_class_owner_type_id, head_hunter_featurer_id, head_hunter_featurer_params
FROM twin_class;

DROP TABLE twin_class;
ALTER TABLE twin_class_temp RENAME TO twin_class;

alter table public.twin_class drop constraint if exists fk_twinclass_create_permission_id;
alter table public.twin_class drop constraint if exists fk_twinclass_edit_permission_id;
alter table public.twin_class drop constraint if exists fk_twinclass_delete_permission_id;
alter table public.twin_class drop constraint if exists fk_twinclass_view_permission_id;
alter table public.twin_class drop constraint if exists fk_twin_class_description_i18n_id;
alter table public.twin_class drop constraint if exists fk_twin_class_name_i18n_id;
alter table public.twin_class drop constraint if exists fk_twin_class_domain_id;
alter table public.twin_class drop constraint if exists fk_twin_class_extends_twin_class_id;
alter table public.twin_class drop constraint if exists fk_twin_class_marker_data_list_id;
alter table public.twin_class drop constraint if exists fk_twin_class_tag_data_list_id;
alter table public.twin_class drop constraint if exists fk_twin_class_twin_class_owner_type_id;
alter table public.twin_class drop constraint if exists fk_twin_class_head_twin_class_id;
alter table public.twin_class drop constraint if exists fk_twin_class_user_id;
alter table public.twin_class drop constraint if exists fk_twin_class_featurer_id;

ALTER TABLE public.twin_class
    ADD CONSTRAINT fk_twinclass_create_permission_id FOREIGN KEY (create_permission_id) REFERENCES permission (id),
    ADD CONSTRAINT fk_twinclass_delete_permission_id FOREIGN KEY (delete_permission_id) REFERENCES permission (id),
    ADD CONSTRAINT fk_twinclass_edit_permission_id FOREIGN KEY (edit_permission_id) REFERENCES permission (id),
    ADD CONSTRAINT fk_twinclass_view_permission_id FOREIGN KEY (view_permission_id) REFERENCES permission (id),
    ADD CONSTRAINT fk_twin_class_description_i18n_id FOREIGN KEY (description_i18n_id) REFERENCES i18n (id),
    ADD CONSTRAINT fk_twin_class_name_i18n_id FOREIGN KEY (name_i18n_id) REFERENCES i18n (id),
    ADD CONSTRAINT fk_twin_class_domain_id FOREIGN KEY (domain_id) REFERENCES domain (id),
    ADD CONSTRAINT fk_twin_class_extends_twin_class_id FOREIGN KEY (extends_twin_class_id) REFERENCES twin_class (id),
    ADD CONSTRAINT fk_twin_class_marker_data_list_id FOREIGN KEY (marker_data_list_id) REFERENCES data_list (id),
    ADD CONSTRAINT fk_twin_class_tag_data_list_id FOREIGN KEY (tag_data_list_id) REFERENCES data_list (id),
    ADD CONSTRAINT fk_twin_class_twin_class_owner_type_id FOREIGN KEY (twin_class_owner_type_id) REFERENCES twin_class_owner_type (id),
    ADD CONSTRAINT fk_twin_class_head_twin_class_id FOREIGN KEY (head_twin_class_id) REFERENCES twin_class (id),
    ADD CONSTRAINT fk_twin_class_user_id FOREIGN KEY (created_by_user_id) REFERENCES "user" (id),
    ADD CONSTRAINT fk_twin_class_featurer_id FOREIGN KEY (head_hunter_featurer_id) REFERENCES featurer (id);

CREATE UNIQUE INDEX twin_class_pk ON twin_class (id);
CREATE UNIQUE INDEX twin_class_domain_id_key_uindex ON twin_class (domain_id, key);
CREATE INDEX twin_class_extends_twin_class_id_index ON twin_class (extends_twin_class_id);
CREATE INDEX twin_class_head_twin_class_id_index ON twin_class (head_twin_class_id);
CREATE INDEX twin_class_marker_data_list_id_index ON twin_class (marker_data_list_id);
CREATE INDEX twin_class_tag_data_list_id_index ON twin_class (tag_data_list_id);
CREATE INDEX twin_class_twin_class_owner_type_id_index ON twin_class (twin_class_owner_type_id);










-- alter table public.twin_class_schema_map drop column create_permission_id;
