package org.twins.core.dao.twin;

import org.cambium.common.EasyLoggable;

import java.util.UUID;

public record TwinNoRelationsProjection(
    UUID id,
    UUID twinClassId,
    UUID headTwinId,
    String hierarchyTree,
    String externalId,
    UUID ownerBusinessAccountId,
    UUID ownerUserId,
    UUID viewPermissionId,
    UUID permissionSchemaSpaceId,
    UUID twinflowSchemaSpaceId,
    UUID twinClassSchemaSpaceId,
    UUID aliasSpaceId,
    UUID twinStatusId,
    String name,
    String description,
    UUID createdByUserId,
    UUID assignerUserId
) implements EasyLoggable {
    @Override
    public String easyLog(Level level) {
        return "twin[" + id + "]";
    }
}
