-- create field type
INSERT INTO public.search_field (id) VALUES ('fieldNumeric') on conflict on constraint search_by_user_type_pk do nothing ;
INSERT INTO public.search_field (id) VALUES ('fieldDate') on conflict on constraint search_by_user_type_pk do nothing ;
INSERT INTO public.search_field (id) VALUES ('fieldText') on conflict on constraint search_by_user_type_pk do nothing ;
INSERT INTO public.search_field (id) VALUES ('fieldList') on conflict on constraint search_by_user_type_pk do nothing ;
INSERT INTO public.search_field (id) VALUES ('fieldId') on conflict on constraint search_by_user_type_pk do nothing ;

-- create new featurer
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2706, 27, 'org.twins.core.featurer.search.criteriabuilder.SearchCriteriaBuilderFieldNumeric', 'Field numeric', '', false) on conflict on constraint featurer_pk do nothing ;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2707, 27, 'org.twins.core.featurer.search.criteriabuilder.SearchCriteriaBuilderFieldDate', 'Field date', '', false) on conflict on constraint featurer_pk do nothing ;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2708, 27, 'org.twins.core.featurer.search.criteriabuilder.SearchCriteriaBuilderFieldId', 'Field id', '', false) on conflict on constraint featurer_pk do nothing ;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2709, 27, 'org.twins.core.featurer.search.criteriabuilder.SearchCriteriaBuilderFieldText', 'Field text', '', false) on conflict on constraint featurer_pk do nothing ;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (2710, 27, 'org.twins.core.featurer.search.criteriabuilder.SearchCriteriaBuilderFieldList', 'Field list', '', false) on conflict on constraint featurer_pk do nothing ;
