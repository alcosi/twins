insert into twin_class (id, domain_id, key, permission_schema_space, abstract, created_by_user_id, created_at, twin_class_owner_type_id, twinflow_schema_space, twin_class_schema_space, alias_space)
    values (
            '38b80f50-f722-4e5a-8fb0-4b5fc04e2c7c',
            'f67ad556-dd27-4871-9a00-16fb0e8a4102',
            'ELPMEE_ANCESTOR',
            false,
            true,
            '608c6d7d-99c8-4d87-89c6-2f72d0f5d673',
            '2023-09-25 14:48:05.581332',
            'domainBusinessAccount',
            false,
            false,
            false) on conflict on constraint twin_class_pk do update set domain_id = excluded.domain_id, key = excluded.key, permission_schema_space = excluded.permission_schema_space,
                                                                         abstract = excluded.abstract, created_by_user_id = excluded.created_by_user_id, created_at = excluded.created_at,
                                                                         twin_class_owner_type_id = excluded.twin_class_owner_type_id, twinflow_schema_space = excluded.twinflow_schema_space,
                                                                         twin_class_schema_space = excluded.twin_class_schema_space, alias_space = excluded.alias_space;

update domain
    set ancestor_twin_class_id='38b80f50-f722-4e5a-8fb0-4b5fc04e2c7c'
        where key like 'elpmee';

UPDATE twin_class
    set extends_twin_class_id='38b80f50-f722-4e5a-8fb0-4b5fc04e2c7c'
        where extends_twin_class_id is null
            and id not in ('00000000-0000-0000-0001-000000000001', '00000000-0000-0000-0001-000000000003')
