insert into face_component(id, face_component_type_id, name, description)
values ('TW006', 'TWIDGET', 'Twin actions', '')
on conflict do nothing;

create table if not exists face_tw006
(
    id                             uuid not null primary key,
    face_id                        uuid not null references face on update cascade on delete restrict,
    twin_pointer_validator_rule_id uuid references twin_pointer_validator_rule on update cascade on delete cascade,
    target_twin_pointer_id         uuid references twin_pointer on update cascade on delete restrict,
    style_classes                  varchar,
    ui_type                        varchar
);

create index if not exists face_tw006_face_id_idx
    on face_tw006 (face_id);

create index if not exists face_tw006_twin_pointer_validator_rule_id_idx
    on face_tw006 (twin_pointer_validator_rule_id);

create index if not exists face_tw006_target_twin_pointer_id_idx
    on face_tw006 (target_twin_pointer_id);


create table if not exists face_tw006_action
(
    id             uuid         not null primary key,
    face_tw006_id  uuid         not null references face_tw006 on update cascade on delete cascade,
    twin_action_id varchar(255) not null references twin_action on update cascade on delete cascade,
    label_i18n_id  uuid references i18n on update cascade on delete restrict
);

create index if not exists face_tw006_action_face_tw_006_id_idx
    on face_tw006_action (face_tw006_id);

create index if not exists face_tw006_action_twin_action_id_idx
    on face_tw006_action (twin_action_id);

create index if not exists face_tw006_action_label_i18n_id_idx
    on face_tw006_action (label_i18n_id);


insert into i18n_type(id, name)
values ('twinAction', 'Twin action')
on conflict do nothing;

insert into i18n_locale (locale, name, active, native_name, icon)
values ('en', 'English', true, 'English', null)
on conflict do nothing;

insert into i18n_locale (locale, name, active, native_name, icon)
values ('pl', 'Polish', true, 'Polski', null)
on conflict do nothing;

insert into i18n_locale (locale, name, active, native_name, icon)
values ('ru', 'Russian', true, 'Русский', null)
on conflict do nothing;

insert into i18n_locale (locale, name, active, native_name, icon)
values ('de', 'German', true, 'Deutsch', null)
on conflict do nothing;

insert into i18n_locale (locale, name, active, native_name, icon)
values ('uk', 'Ukrainian', true, 'Українська', null)
on conflict do nothing;

insert into i18n_locale (locale, name, active, native_name, icon)
values ('fr', 'French', true, 'Français', null)
on conflict do nothing;

insert into i18n_locale (locale, name, active, native_name, icon)
values ('it', 'Italian', true, 'Italiano', null)
on conflict do nothing;

insert into i18n_locale (locale, name, active, native_name, icon)
values ('es', 'Spanish', true, 'Español', null)
on conflict do nothing;

insert into i18n_locale (locale, name, active, native_name, icon)
values ('cs', 'Czech', true, 'Čeština', null)
on conflict do nothing;


insert into i18n(id, name, key, i18n_type_id, domain_id)
values ('00000000-0000-0000-0012-000000000039', 'Edit', 'twin.actions.edit', 'twinAction', null),
       ('00000000-0000-0000-0012-000000000040', 'Delete', 'twin.actions.delete', 'twinAction', null),
       ('00000000-0000-0000-0012-000000000041', 'Comment', 'twin.actions.comment', 'twinAction', null),
       ('00000000-0000-0000-0012-000000000042', 'Move', 'twin.actions.move', 'twinAction', null),
       ('00000000-0000-0000-0012-000000000043', 'Watch', 'twin.actions.watch', 'twinAction', null),
       ('00000000-0000-0000-0012-000000000044', 'Time tracking', 'twin.actions.time_track', 'twinAction', null),
       ('00000000-0000-0000-0012-000000000045', 'Add attachment', 'twin.actions.attachment_add', 'twinAction', null),
       ('00000000-0000-0000-0012-000000000046', 'View history', 'twin.actions.history_view', 'twinAction', null)
on conflict do nothing;


insert into i18n_translation(i18n_id, locale, translation, usage_counter)
values ('00000000-0000-0000-0012-000000000039', 'en', 'Edit', 0),
--        ('00000000-0000-0000-0012-000000000039', 'cs', 'Upravit', 0),
--        ('00000000-0000-0000-0012-000000000039', 'fr', 'Modifier', 0),
--        ('00000000-0000-0000-0012-000000000039', 'ru', 'Редактировать', 0),
--        ('00000000-0000-0000-0012-000000000039', 'de', 'Bearbeiten', 0),
--        ('00000000-0000-0000-0012-000000000039', 'it', 'Modifica', 0),
--        ('00000000-0000-0000-0012-000000000039', 'pl', 'Edytuj', 0),
--        ('00000000-0000-0000-0012-000000000039', 'es', 'Editar', 0),
--        ('00000000-0000-0000-0012-000000000039', 'uk', 'Редагувати', 0),

       ('00000000-0000-0000-0012-000000000040', 'en', 'Delete', 0),
--        ('00000000-0000-0000-0012-000000000038', 'cs', 'Smazat', 0),
--        ('00000000-0000-0000-0012-000000000038', 'fr', 'Supprimer', 0),
--        ('00000000-0000-0000-0012-000000000038', 'ru', 'Удалить', 0),
--        ('00000000-0000-0000-0012-000000000038', 'de', 'Löschen', 0),
--        ('00000000-0000-0000-0012-000000000038', 'it', 'Elimina', 0),
--        ('00000000-0000-0000-0012-000000000038', 'pl', 'Usuń', 0),
--        ('00000000-0000-0000-0012-000000000038', 'es', 'Eliminar', 0),
--        ('00000000-0000-0000-0012-000000000038', 'uk', 'Видалити', 0),

       ('00000000-0000-0000-0012-000000000041', 'en', 'Comment', 0),
--        ('00000000-0000-0000-0012-000000000039', 'cs', 'Komentovat', 0),
--        ('00000000-0000-0000-0012-000000000039', 'fr', 'Commenter', 0),
--        ('00000000-0000-0000-0012-000000000039', 'ru', 'Комментировать', 0),
--        ('00000000-0000-0000-0012-000000000039', 'de', 'Kommentieren', 0),
--        ('00000000-0000-0000-0012-000000000039', 'it', 'Сommentare', 0),
--        ('00000000-0000-0000-0012-000000000039', 'pl', 'Komentować', 0),
--        ('00000000-0000-0000-0012-000000000039', 'es', 'Comentar', 0),
--        ('00000000-0000-0000-0012-000000000039', 'uk', 'Коментувати', 0),

       ('00000000-0000-0000-0012-000000000042', 'en', 'Move', 0),
--        ('00000000-0000-0000-0012-000000000040', 'cs', 'Přesunout', 0),
--        ('00000000-0000-0000-0012-000000000040', 'fr', 'Déplacer', 0),
--        ('00000000-0000-0000-0012-000000000040', 'ru', 'Переместить', 0),
--        ('00000000-0000-0000-0012-000000000040', 'de', 'Verschieben', 0),
--        ('00000000-0000-0000-0012-000000000040', 'it', 'Spostare', 0),
--        ('00000000-0000-0000-0012-000000000040', 'pl', 'Przenosić', 0),
--        ('00000000-0000-0000-0012-000000000040', 'es', 'Mover', 0),
--        ('00000000-0000-0000-0012-000000000040', 'uk', 'Переміщати', 0),

       ('00000000-0000-0000-0012-000000000043', 'en', 'Watch', 0),
--        ('00000000-0000-0000-0012-000000000041', 'cs', 'Dívat se', 0),
--        ('00000000-0000-0000-0012-000000000041', 'fr', 'Regarder', 0),
--        ('00000000-0000-0000-0012-000000000041', 'ru', 'Смотреть', 0),
--        ('00000000-0000-0000-0012-000000000041', 'de', 'Schauen', 0),
--        ('00000000-0000-0000-0012-000000000041', 'it', 'Guardare', 0),
--        ('00000000-0000-0000-0012-000000000041', 'pl', 'Patrzeć', 0),
--        ('00000000-0000-0000-0012-000000000041', 'es', 'Mirar', 0),
--        ('00000000-0000-0000-0012-000000000041', 'uk', 'Дивитися', 0),

       ('00000000-0000-0000-0012-000000000044', 'en', 'Time tracking', 0),
--        ('00000000-0000-0000-0012-000000000042', 'cs', 'Sledování času', 0),
--        ('00000000-0000-0000-0012-000000000042', 'fr', 'Suivi du temps', 0),
--        ('00000000-0000-0000-0012-000000000042', 'ru', 'Отслеживание времени', 0),
--        ('00000000-0000-0000-0012-000000000042', 'de', 'Zeiterfassung', 0),
--        ('00000000-0000-0000-0012-000000000042', 'it', 'Monitoraggio del tempo', 0),
--        ('00000000-0000-0000-0012-000000000042', 'pl', 'Śledzenie czasu', 0),
--        ('00000000-0000-0000-0012-000000000042', 'es', 'Seguimiento del tiempo', 0),
--        ('00000000-0000-0000-0012-000000000042', 'uk', 'Відстеження часу', 0),

       ('00000000-0000-0000-0012-000000000045', 'en', 'Add attachment', 0),
--        ('00000000-0000-0000-0012-000000000043', 'cs', 'Přidat přílohu', 0),
--        ('00000000-0000-0000-0012-000000000043', 'fr', 'Ajouter une pièce jointe', 0),
--        ('00000000-0000-0000-0012-000000000043', 'ru', 'Добавить вложение', 0),
--        ('00000000-0000-0000-0012-000000000043', 'de', 'Anhang hinzufügen', 0),
--        ('00000000-0000-0000-0012-000000000043', 'it', 'Aggiungi allegato', 0),
--        ('00000000-0000-0000-0012-000000000043', 'pl', 'Dodaj załącznik', 0),
--        ('00000000-0000-0000-0012-000000000043', 'es', 'Añadir adjunto', 0),
--        ('00000000-0000-0000-0012-000000000043', 'uk', 'Додати вкладення', 0),

       ('00000000-0000-0000-0012-000000000046', 'en', 'View history', 0)
--        ('00000000-0000-0000-0012-000000000044', 'cs', 'Zobrazit historii', 0),
--        ('00000000-0000-0000-0012-000000000044', 'fr', 'Voir l''historique', 0),
--        ('00000000-0000-0000-0012-000000000044', 'ru', 'Просмотреть историю', 0),
--        ('00000000-0000-0000-0012-000000000044', 'de', 'Verlauf anzeigen', 0),
--        ('00000000-0000-0000-0012-000000000044', 'it', 'Visualizza cronologia', 0),
--        ('00000000-0000-0000-0012-000000000044', 'pl', 'Zobacz historię', 0),
--        ('00000000-0000-0000-0012-000000000044', 'es', 'Ver historial', 0),
--        ('00000000-0000-0000-0012-000000000044', 'uk', 'Переглянути історію', 0)
on conflict do nothing;


alter table twin_action
    add column if not exists name_i18n_id uuid references i18n on update cascade on delete restrict;

create index if not exists twin_action_i18n_id_idx
    on twin_action (name_i18n_id);

update twin_action
set name_i18n_id='00000000-0000-0000-0012-000000000039'
where twin_action.id = 'EDIT';

update twin_action
set name_i18n_id='00000000-0000-0000-0012-000000000040'
where twin_action.id = 'DELETE';

update twin_action
set name_i18n_id='00000000-0000-0000-0012-000000000041'
where twin_action.id = 'COMMENT';

update twin_action
set name_i18n_id='00000000-0000-0000-0012-000000000042'
where twin_action.id = 'MOVE';

update twin_action
set name_i18n_id='00000000-0000-0000-0012-000000000043'
where twin_action.id = 'WATCH';

update twin_action
set name_i18n_id='00000000-0000-0000-0012-000000000044'
where twin_action.id = 'TIME_TRACK';

update twin_action
set name_i18n_id='00000000-0000-0000-0012-000000000045'
where twin_action.id = 'ATTACHMENT_ADD';

update twin_action
set name_i18n_id='00000000-0000-0000-0012-000000000046'
where twin_action.id = 'HISTORY_VIEW';

alter table twin_action
    alter column name_i18n_id set not null;
