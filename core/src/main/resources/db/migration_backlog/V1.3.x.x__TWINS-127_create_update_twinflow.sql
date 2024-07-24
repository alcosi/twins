alter table public.twin_alias drop constraint twin_alias_twin_id_fkey;
alter table public.twin_alias add foreign key (twin_id) references public.twin on delete cascade;

alter table public.twin_alias drop constraint twin_alias_user_id_fkey;
alter table public.twin_alias add foreign key (user_id) references public."user" on delete cascade;

alter table public.twin_alias drop constraint twin_alias_domain_id_fkey;
alter table public.twin_alias add foreign key (domain_id) references public.domain on delete cascade;

alter table public.twin_alias drop constraint twin_alias_business_account_id_fkey;
alter table public.twin_alias add foreign key (business_account_id) references public.business_account on delete cascade;
