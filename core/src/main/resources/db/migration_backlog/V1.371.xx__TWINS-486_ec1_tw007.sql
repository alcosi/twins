insert into i18n(id, key, i18n_type_id, domain_id)
select 'ea3fe0af-285e-4cbb-817f-e228995a0f1d', key, i18n_type_id, domain_id
from i18n
where i18n_type_id='faceElement' and domain_id is not null limit 1
on conflict do nothing;

insert into i18n_translation(i18n_id, locale, translation, usage_counter)
values ('ea3fe0af-285e-4cbb-817f-e228995a0f1d', 'en', 'Save', 0)
on conflict do nothing;

alter table face_tw007
    add column if not exists save_changes_label_i18n_id uuid not null references i18n on update cascade on delete restrict default 'ea3fe0af-285e-4cbb-817f-e228995a0f1d';

create index if not exists face_tw007_save_changes_label_i18n_id
    on face_tw007 (save_changes_label_i18n_id);

