-- fix error in V1.3.159.01__TWINS-283_at1_twin_class_owner_type.sql
UPDATE public.domain_type_twin_class_owner_type SET twin_class_owner_type_id = 'domainUser'::varchar WHERE domain_type_id LIKE 'basic' AND twin_class_owner_type_id LIKE 'businessAccount';
UPDATE public.domain_type_twin_class_owner_type SET twin_class_owner_type_id = 'domain'::varchar WHERE domain_type_id LIKE 'basic' AND twin_class_owner_type_id LIKE 'user';
