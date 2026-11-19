-- TWINS-943: register twin finder featurers (SearchCriteriaBuilder, featurer_type_id = 27).
-- Class/name/description mirror the @Featurer annotations (single source of truth is the code).

insert into featurer (id, featurer_type_id, class, name, description, deprecated)
values (2729, 27, 'org.twins.core.featurer.twin.finder.TwinFinderByHeadOfSubquery',
        'By head of subquery twins',
        'Keeps twins that are the head twin of twins returned by a saved search.', false)
    on conflict (id) do nothing;

insert into featurer (id, featurer_type_id, class, name, description, deprecated)
values (2730, 27, 'org.twins.core.featurer.twin.finder.TwinFinderBySpaceRoleUserRequested',
        'By space role user id (requested)',
        'Keeps twins of the users joined to the requested twin''s space', false)
    on conflict (id) do nothing;

insert into featurer (id, featurer_type_id, class, name, description, deprecated)
values (2731, 27, 'org.twins.core.featurer.twin.finder.TwinFinderByLinkedTwinsRequested',
        'Linked twins of requested twin',
        'Keeps twins linked to the requested twin in either direction (twin_link)', false)
    on conflict (id) do nothing;
