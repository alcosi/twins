-- TWINS-942: public flag marks resources available via the public download API (GET /public/resource/{id}/v1)
alter table resource
    add column if not exists public boolean not null default true;

comment on column resource.public is 'Resource is available via the public download API (GET /public/resource/{id}/v1)';
