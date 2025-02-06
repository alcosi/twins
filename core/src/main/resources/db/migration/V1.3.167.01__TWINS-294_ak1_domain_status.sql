CREATE TABLE if not exists public.domain_status (
                               id VARCHAR PRIMARY KEY,
                               description TEXT
);

INSERT INTO  public.domain_status (id, description) VALUES
                                                ('ACTIVE', 'Active status'),
                                                ('DISABLED', 'Disabled status') on conflict (id) do nothing;

alter table  public.domain add column if not exists domain_status_id varchar not null default 'ACTIVE';

alter table  public.domain DROP CONSTRAINT IF EXISTS domain_domain_status_fk;

ALTER TABLE public.domain
    ADD CONSTRAINT domain_domain_status_fk
        FOREIGN KEY (domain_status_id) REFERENCES public.domain_status;
