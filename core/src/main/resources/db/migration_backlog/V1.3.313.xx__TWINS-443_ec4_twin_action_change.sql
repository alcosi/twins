alter table twin_action
    add column i18n_id uuid references i18n on update cascade on delete restrict;

create index twin_action_i18n_id_idx on twin_action (i18n_id);

update twin_action
set i18n_id=(select id from i18n where name='Edit')
where twin_action.id='EDIT';

update twin_action
set i18n_id=(select id from i18n where name='Delete')
where twin_action.id='DELETE';

update twin_action
set i18n_id=(select id from i18n where name='Comment')
where twin_action.id='COMMENT';

update twin_action
set i18n_id=(select id from i18n where name='Move')
where twin_action.id='MOVE';

update twin_action
set i18n_id=(select id from i18n where name='Watch')
where twin_action.id='WATCH';

update twin_action
set i18n_id=(select id from i18n where name='Time tracking')
where twin_action.id='TIME_TRACK';

update twin_action
set i18n_id=(select id from i18n where name='Add attachment')
where twin_action.id='ATTACHMENT_ADD';

update twin_action
set i18n_id=(select id from i18n where name='View history')
where twin_action.id='HISTORY_VIEW';
