-- is assignee
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2404, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerApiUserIsAssignee', 'ConditionerApiUserIsAssignee', '', false) on conflict (id) do nothing;
-- is member of any groups
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2423, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerApiUserIsMemberOfGroup', 'ConditionerApiUserIsMemberOfGroup', '', false) on conflict (id) do nothing;
-- check field in factory item head twin field equals to parameter value
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2424, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerHeadTwinFieldValueEquals', 'ConditionerHeadTwinFieldValueEquals', '', false) on conflict (id) do nothing;
-- assignee of factory item twin equals assignee of twin by context field link.
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2425, 24, 'org.twins.core.featurer.factory.conditioner.ConditionerFactoryItemTwinAssigneeEqualsContextTwinFieldLinkAssignee', 'ConditionerFactoryItemTwinAssigneeEqualsContextTwinFieldLinkAssignee', '', false) on conflict (id) do nothing;
