INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated)
VALUES (1609, 16, 'org.twins.core.featurer.twin.validator.TransitionValidatorTwinInStatuses', 'TransitionValidatorTwinInStatuses', '', false)
on conflict on constraint featurer_pk do nothing ;
