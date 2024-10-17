
CREATE TABLE  if not exists public.twin_validator_set (
                                                   id uuid NOT NULL,
                                                   domain_id uuid,
                                                   name character varying,
                                                   description character varying
);
CREATE TABLE if not exists public.twin_validator (
                                                      id uuid NOT NULL,
                                                      twin_validator_set_id uuid NOT NULL,
                                                      twin_validator_featurer_id integer NOT NULL,
                                                      twin_validator_params public.hstore,
                                                      invert boolean DEFAULT false,
                                                      active boolean DEFAULT true,
                                                      description character varying

);
CREATE INDEX if not exists twin_validator_featurer_id_i ON public.twin_validator USING btree (twin_validator_featurer_id);
ALTER TABLE public.twin_action_validator ADD if not exists twin_validator_set_id UUID;
ALTER TABLE public.twin_comment_action_alien_validator ADD if not exists twin_validator_set_id UUID;
ALTER TABLE public.twinflow_transition_validator ADD if not exists twin_validator_set_id UUID;

alter table public.twin_action_validator drop column if exists twin_validator_featurer_id;
alter table public.twin_action_validator drop column if exists twin_validator_params;
alter table public.twin_action_validator drop column if exists invert;
alter table public.twin_action_validator drop column if exists active;
alter table public.twin_comment_action_alien_validator drop column if exists twin_validator_featurer_id;
alter table public.twin_comment_action_alien_validator drop column if exists twin_validator_params;
alter table public.twin_comment_action_alien_validator drop column if exists invert;
alter table public.twin_comment_action_alien_validator drop column if exists active;
alter table public.twinflow_transition_validator drop column if exists twin_validator_featurer_id;
alter table public.twinflow_transition_validator drop column if exists twin_validator_params;
alter table public.twinflow_transition_validator drop column if exists invert;
alter table public.twinflow_transition_validator drop column if exists active;

alter table public.twin_action_validator drop constraint if exists twin_action_validator_twin_validator_set_id_fk;
alter table public.twin_comment_action_alien_validator drop constraint if exists comment_action_alien_twin_validator_set_id_fk;
alter table public.twinflow_transition_validator drop constraint if exists twinflow_transition_validator_twin_validator_set_id_fk;
alter table public.twin_validator drop constraint if exists twin_validator_pk;
alter table public.twin_validator drop constraint if exists twin_validator_featurer_id_fk;
alter table public.twin_validator drop constraint if exists twin_validator_twin_validator_set_id_fk;
alter table public.twin_validator_set drop constraint if exists twin_validator_set_pk;
alter table public.twin_validator_set drop constraint if exists twin_validator_set_domain_id_fk;

ALTER TABLE ONLY public.twin_validator_set ADD CONSTRAINT twin_validator_set_pk PRIMARY KEY (id);
ALTER TABLE ONLY public.twin_validator_set ADD CONSTRAINT twin_validator_set_domain_id_fk FOREIGN KEY (domain_id) REFERENCES public.domain(id);
ALTER TABLE ONLY public.twin_validator ADD CONSTRAINT twin_validator_pk PRIMARY KEY (id);
ALTER TABLE ONLY public.twin_validator ADD CONSTRAINT twin_validator_featurer_id_fk FOREIGN KEY (twin_validator_featurer_id) REFERENCES public.featurer(id);
ALTER TABLE ONLY public.twin_validator ADD CONSTRAINT twin_validator_twin_validator_set_id_fk FOREIGN KEY (twin_validator_set_id) REFERENCES public.twin_validator_set(id);
ALTER TABLE ONLY public.twin_action_validator ADD CONSTRAINT twin_action_validator_twin_validator_set_id_fk FOREIGN KEY (twin_validator_set_id) REFERENCES public.twin_validator_set(id);
ALTER TABLE ONLY public.twin_comment_action_alien_validator ADD CONSTRAINT comment_action_alien_twin_validator_set_id_fk FOREIGN KEY (twin_validator_set_id) REFERENCES public.twin_validator_set(id);
ALTER TABLE ONLY public.twinflow_transition_validator ADD CONSTRAINT twinflow_transition_validator_twin_validator_set_id_fk FOREIGN KEY (twin_validator_set_id) REFERENCES public.twin_validator_set(id);
