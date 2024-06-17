CREATE TABLE if not exists public.twin_alias_type (
              alias_type_id char NOT NULL,
              description character varying(255),
              CONSTRAINT twin_alias_type_pk PRIMARY KEY (alias_type_id)
);

INSERT INTO public.twin_alias_type VALUES ('D', '') on conflict(alias_type_id) do nothing;
INSERT INTO public.twin_alias_type VALUES ('C', '') on conflict(alias_type_id) do nothing;
INSERT INTO public.twin_alias_type VALUES ('B', '') on conflict(alias_type_id) do nothing;
INSERT INTO public.twin_alias_type VALUES ('S', '') on conflict(alias_type_id) do nothing;
INSERT INTO public.twin_alias_type VALUES ('T', '') on conflict(alias_type_id) do nothing;
INSERT INTO public.twin_alias_type VALUES ('K', '') on conflict(alias_type_id) do nothing;


CREATE TABLE IF NOT EXISTS public.twin_alias (
                                                 id UUID PRIMARY KEY,
                                                 alias_value VARCHAR(255) NOT NULL,
                                                 twin_alias_type_id VARCHAR(1) NOT NULL,
                                                 twin_id UUID,
                                                 user_id UUID,
                                                 domain_id UUID,
                                                 business_account_id UUID,
                                                 FOREIGN KEY (twin_alias_type_id) REFERENCES public.twin_alias_type(alias_type_id),
                                                 FOREIGN KEY (twin_id) REFERENCES public.twin(id),
                                                 FOREIGN KEY (user_id) REFERENCES public.user(id),
                                                 FOREIGN KEY (domain_id) REFERENCES public.domain(id),
                                                 FOREIGN KEY (business_account_id) REFERENCES public.business_account(id)
);
CREATE UNIQUE INDEX if not exists twin_alias_D_C_S ON twin_alias (domain_id, alias_value) WHERE twin_alias_type_id IN ('D', 'C', 'S');
CREATE UNIQUE INDEX if not exists twin_alias_B_K ON twin_alias (domain_id, business_account_id, alias_value) WHERE twin_alias_type_id IN ('B', 'K');
CREATE UNIQUE INDEX if not exists twin_alias_T ON twin_alias (domain_id, user_id, alias_value) WHERE twin_alias_type_id = 'T';
alter table public.twin_class add if not exists alias_counter integer default 0;
