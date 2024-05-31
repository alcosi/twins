UPDATE public.featurer_type SET name = 'DomainInitiator' WHERE id = 25;
INSERT INTO public.featurer_type (id, name, description) VALUES (27, 'SearchCriteriaBuilder', '') on conflict (id) do nothing;
DELETE FROM public.featurer_param WHERE featurer_id = 2503 AND key LIKE 'dstTwinId' ESCAPE '#';
DELETE FROM public.featurer_param WHERE featurer_id = 2501 AND key LIKE 'entityId' ESCAPE '#';
DELETE FROM public.featurer_param WHERE featurer_id = 2504 AND key LIKE 'required' ESCAPE '#';
DELETE FROM public.featurer_param WHERE featurer_id = 2504 AND key LIKE 'paramKey' ESCAPE '#';
DELETE FROM public.featurer_param WHERE featurer_id = 2503 AND key LIKE 'linkId' ESCAPE '#';
UPDATE public.featurer SET class = 'org.twins.core.featurer.search.criteriabuilder.DomainInitiatorBasic', name = 'DomainInitiatorBasic' WHERE id = 2501;
UPDATE public.featurer SET class = 'org.twins.core.featurer.search.criteriabuilder.DomainInitiatorB2B', name = 'DomainInitiatorB2B' WHERE id = 2502;
