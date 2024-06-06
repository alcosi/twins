INSERT INTO public.featurer (id, featurer_type_id, class, name, description) VALUES (2705::integer, 27::integer, 'org.twins.core.featurer.search.criteriabuilder.SearchCriteriaBuilderParamLinkDst'::varchar, 'SearchCriteriaBuilderParamLinkDst'::varchar, ''::varchar(255)) on conflict(id) do nothing;
INSERT INTO search_field (id) VALUES ('linkId') on conflict do nothing;
