package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;


@Data
@Accessors(chain = true)
@FieldNameConstants
public class PermissionSchemaUserSearch {
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
