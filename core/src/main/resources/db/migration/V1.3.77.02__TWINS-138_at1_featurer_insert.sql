-- create featurer TransitionTriggerClearCurrentUserTouch and TransitionTriggerClearAllUsersTouch
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (1502, 15, 'org.twins.core.featurer.transition.trigger.TransitionTriggerClearCurrentUserTouch', 'TransitionTriggerClearCurrentUserTouch', '', false) on conflict on constraint featurer_pk do nothing;
INSERT INTO public.featurer (id, featurer_type_id, class, name, description, deprecated) VALUES (1503, 15, 'org.twins.core.featurer.transition.trigger.TransitionTriggerClearAllUsersTouch', 'TransitionTriggerClearAllUsersTouch', '', false) on conflict on constraint featurer_pk do nothing;