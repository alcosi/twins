package org.cambium.common.util;

import org.jetbrains.annotations.NotNull;
import org.twins.core.service.permission.Permissions;

import java.util.Arrays;
import java.util.UUID;

public class GenerateMigrationForPermissions {
    static final String startId = "00000000-0000-0000-0004-000000000034";
    static final String endId = "00000000-0000-0000-0004-000000000167";
    static final String userGroupId = "00000000-0000-0000-0006-000000000001";
    static final String grantedByUserIdId = "00000000-0000-0000-0000-000000000000";

    public static void main(String[] args) {
        Arrays.asList(Permissions.values()).forEach(permission -> {


            if (permission.getId().compareTo(UUID.fromString(startId)) < 0 ||
                    permission.getId().compareTo(UUID.fromString(endId)) > 0
            ) {
                System.out.println("--Skipping " + permission.name());
                return;
            }
            UUID permissionNameI18Id = UUID.nameUUIDFromBytes(("PermissionName " + permission.name()).getBytes());
            UUID permissionDescriptionI18Id = UUID.nameUUIDFromBytes(("PermissionDescription " + permission.name()).getBytes());
            String permissionNameI18 = capitalize(permission.name().replace("_", " ").toLowerCase());


            String i18nInsert = """
                    --Start {permissionName}
                    insert into public.i18n (id, name, key, i18n_type_id) VALUES
                    ('{permissionNameI18Id}', '{permissionNameI18} name', null, 'permissionName'),
                    ('{permissionDescriptionI18Id}', '{permissionNameI18} description', null, 'permissionDescription')
                    on conflict (id) do update set name = excluded.name, key = excluded.key, i18n_type_id = excluded.i18n_type_id;
                    """
                    .replace("{permissionNameI18Id}", permissionNameI18Id.toString())
                    .replace("{permissionName}", permission.name())
                    .replace("{permissionDescriptionI18Id}", permissionDescriptionI18Id.toString())
                    .replace("{permissionNameI18}", permissionNameI18);


            String translationInsert = """
                    insert into public.i18n_translation (i18n_id, locale, translation, usage_counter) VALUES
                    ('{permissionNameI18Id}', 'en', '{permissionNameI18}', 0),
                    ('{permissionDescriptionI18Id}', 'en', '{permissionNameI18}', 0)
                    on conflict (i18n_id,locale) do update set translation = excluded.translation, usage_counter = excluded.usage_counter;;
                    """
                    .replace("{permissionNameI18Id}", permissionNameI18Id.toString())
                    .replace("{permissionDescriptionI18Id}", permissionDescriptionI18Id.toString())
                    .replace("{permissionNameI18}", permissionNameI18);

            String permissionInsert = """
                    INSERT INTO public.permission (id, key, permission_group_id, name_i18n_id, description_i18n_id) VALUES
                    ('{permissionId}', '{permissionName}', '{permissionGroupId}', '{permissionNameI18Id}', '{permissionDescriptionI18Id}')
                    on conflict (id) do update set key=excluded.key;
                    """
                    .replace("{permissionId}", permission.getId().toString())
                    .replace("{permissionGroupId}", permission.getPermissionGroupId().toString())
                    .replace("{permissionName}", permission.name())
                    .replace("{permissionNameI18Id}", permissionNameI18Id.toString())
                    .replace("{permissionDescriptionI18Id}", permissionDescriptionI18Id.toString())
                    .replace("{permissionNameI18}", permissionNameI18);
            String grantInsert = """
                    INSERT INTO public.permission_grant_global
                    (id, permission_id, user_group_id, granted_by_user_id, granted_at) VALUES 
                    ('{grantId}', '{permissionId}', '{userGroupId}', '{grantedByUserIdId}', default) 
                    on conflict do nothing ;
                    --End {permissionName}
                    """
                    .replace("{permissionId}", permission.getId().toString())
                    .replace("{userGroupId}", userGroupId)
                    .replace("{grantedByUserIdId}", grantedByUserIdId)
                    .replace("{permissionName}", permission.name())
                    .replace("{grantId}", permission.getId().toString().replace("00000000-0000-0000-0004", "00000000-0000-0000-0007"));
            System.out.println(i18nInsert);
            System.out.println(translationInsert);
            System.out.println(permissionInsert);
            System.out.println(grantInsert);
            System.out.println();
        });
    }

    @NotNull
    private static String capitalize(String permissionNameI18) {
        return permissionNameI18.substring(0, 1).toUpperCase() + permissionNameI18.substring(1);
    }
}
