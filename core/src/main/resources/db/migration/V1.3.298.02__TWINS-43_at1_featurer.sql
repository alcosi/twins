-- new featurer 1332
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (1332, 13, 'org.twins.core.featurer.fieldtyper.FieldTyperSpaceRoleUsers', 'Space role users', '', false) on conflict on constraint featurer_pk do nothing ;
