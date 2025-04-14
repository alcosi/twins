INSERT INTO public.history_type VALUES ('externalIdChanged', 'Field ''${field.name}'' was set with ''${toValue}''', 'softEnabled') on conflict on constraint history_type_pkey do nothing ;
