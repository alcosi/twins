alter table twin_recompute_subscriber
    drop constraint if exists twin_recompute_subscriber_pointer_field_uk;

drop index if exists twin_recompute_subscriber_pointer_field_uk;

create index if not exists twin_recompute_subscriber_pointer_field_uk
    on twin_recompute_subscriber (subscriber_twin_pointer_id, subscriber_twin_class_field_id);