INSERT INTO featurer_type (id, name) VALUES (35, 'Trustor') ON CONFLICT (id) DO NOTHING;

insert into featurer(id, featurer_type_id, class, name, description)
values (3501, 35, '', '', '')
on conflict (id) do nothing;

alter table identity_provider
    add if not exists trustor_featurer_id integer
        constraint identity_provider_featurer_id_fk
            references featurer
            on update cascade;

alter table identity_provider
    add if not exists trustor_params hstore;

update identity_provider set trustor_featurer_id = 3501 where trustor_featurer_id is null;

alter table identity_provider
    alter column trustor_featurer_id set not null;