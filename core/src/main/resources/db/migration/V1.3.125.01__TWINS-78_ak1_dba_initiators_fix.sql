DELETE FROM public.featurer_param WHERE featurer_id = 1101::integer AND key LIKE 'twinflowSchemaId' ESCAPE '#';
DELETE FROM public.featurer_param WHERE featurer_id = 1102::integer AND key LIKE 'twinClassSchemaId' ESCAPE '#';
DELETE FROM public.featurer_param WHERE featurer_id = 1102::integer AND key LIKE 'permissionSchemaId' ESCAPE '#';
DELETE FROM public.featurer_param WHERE featurer_id = 1101::integer AND key LIKE 'permissionSchemaId' ESCAPE '#';
DELETE FROM public.featurer_param WHERE featurer_id = 1101::integer AND key LIKE 'twinClassSchemaId' ESCAPE '#';
DELETE FROM public.featurer_param WHERE featurer_id = 1102::integer AND key LIKE 'twinflowSchemaId' ESCAPE '#';
DELETE FROM public.featurer_param WHERE featurer_id = 1102::integer AND key LIKE 'businessAccountTemplateTwinId' ESCAPE '#';
DELETE FROM public.featurer WHERE id = 1102::integer;
DELETE FROM public.featurer WHERE id = 1103::integer;
UPDATE public.featurer SET class = 'org.twins.core.featurer.businessaccount.initiator.BusinessAccountInitiatorImpl'::varchar, name = 'BusinessAccountInitiatorImpl'::varchar WHERE id = 1101::integer;
