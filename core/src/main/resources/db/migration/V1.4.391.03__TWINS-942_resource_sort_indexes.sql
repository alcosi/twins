-- TWINS-942: composite (domain_id, <sort column>) indexes for resource search sortable fields.
-- Search is always domain-scoped, created_at is the default sort field.
create index if not exists idx_resource_domain_id_created_at on resource (domain_id, created_at);
create index if not exists idx_resource_domain_id_original_file_name on resource (domain_id, original_file_name);
create index if not exists idx_resource_domain_id_size_in_bytes on resource (domain_id, size_in_bytes);
