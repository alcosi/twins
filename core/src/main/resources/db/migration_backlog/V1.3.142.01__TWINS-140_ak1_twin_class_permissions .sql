alter table public.twin_class add column if not exists create_permission_id uuid;
alter table public.twin_class add column if not exists edit_permission_id uuid;
alter table public.twin_class add column if not exists delete_permission_id uuid;

alter table public.twin_class drop constraint if exists fk_twinclass_create_permission_id;
alter table public.twin_class drop constraint if exists fk_twinclass_edit_permission_id;
alter table public.twin_class drop constraint if exists fk_twinclass_delete_permission_id;
ALTER TABLE ONLY public.twin_class ADD CONSTRAINT fk_twinclass_create_permission_id FOREIGN KEY (create_permission_id) REFERENCES public.permission(id);
ALTER TABLE ONLY public.twin_class ADD CONSTRAINT fk_twinclass_edit_permission_id FOREIGN KEY (edit_permission_id) REFERENCES public.permission(id);
ALTER TABLE ONLY public.twin_class ADD CONSTRAINT fk_twinclass_delete_permission_id FOREIGN KEY (delete_permission_id) REFERENCES public.permission(id);



















-- alter table public.twin_class_schema_map drop column create_permission_id;
