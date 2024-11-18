package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;


@Data
@Accessors(chain = true)
@FieldNameConstants
public class PermissionGrantUserSearch {
    Set<UUID> idList;
    Set<UUID> idExcludeList;
    Set<UUID> permissionIdList;
    Set<UUID> permissionIdExcludeList;
    Set<UUID> permissionSchemaIdList;
    Set<UUID> permissionSchemaIdExcludeList;
    Set<UUID> userIdList;
    Set<UUID> userIdExcludeList;
    Set<UUID> grantedByUserIdList;
    Set<UUID> grantedByUserIdExcludeList;
}
