insert into featurer(id, featurer_type_id, class, name, description, deprecated)
values (1616, 16, 'org.twins.core.featurer.twin.validator.TwinValidatorTwinHasLinkAndDstTwinHasStatus', 'Twin has link and dst twin has status', '', false)
on conflict do nothing;
