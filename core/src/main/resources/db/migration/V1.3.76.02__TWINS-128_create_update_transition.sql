alter table public.twinflow_transition drop constraint if exists twinflow_transition_i18n_id_fk;
alter table public.twinflow drop constraint if exists twinflow_name_i18n_id_fk;
alter table public.twinflow drop constraint if exists twinflow_description_i18n_id_fk;
alter table public.twinflow_transition drop constraint if exists twinflow_transition_name_i18n_id_fk;
alter table public.twinflow_transition drop constraint if exists twinflow_transition_description_i18n_id_fk;

alter table public.twinflow_transition add if not exists description_i18n_id uuid;

ALTER TABLE public.twinflow ADD CONSTRAINT twinflow_name_i18n_id_fk FOREIGN KEY (name_i18n_id) REFERENCES public.i18n(id) on update cascade;
ALTER TABLE public.twinflow ADD CONSTRAINT twinflow_description_i18n_id_fk FOREIGN KEY (description_i18n_id) REFERENCES  public.i18n(id) on update cascade;;

ALTER TABLE public.twinflow_transition ADD CONSTRAINT twinflow_transition_name_i18n_id_fk FOREIGN KEY (name_i18n_id) REFERENCES public.i18n(id) on update cascade;
ALTER TABLE public.twinflow_transition ADD CONSTRAINT twinflow_transition_description_i18n_id_fk FOREIGN KEY (description_i18n_id) REFERENCES  public.i18n(id) on update cascade;

