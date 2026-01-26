alter table  twin_class_field
add column if not exists fe_validation_error_i18n_id uuid references i18n (id) on update cascade;
alter table  twin_class_field
add column if not exists be_validation_error_i18n_id uuid references i18n (id) on update cascade;

