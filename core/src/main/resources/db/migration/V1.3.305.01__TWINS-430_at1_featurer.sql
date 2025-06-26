-- new featurer
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (1612, 16, 'org.twins.core.featurer.twin.validator.TwinValidatorApiUserIsMemberOfSpace', 'Current user is member of space', '', false) on conflict do nothing ;
