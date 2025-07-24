package org.twins.core.dao.twin;

import org.cambium.common.EasyLoggable;

import java.util.UUID;

public interface TwinNoRelationsProjection extends EasyLoggable {

    UUID getId();
    UUID getTwinClassId();
    UUID getHeadTwinId();
    String getHierarchyTree();
    String getExternalId();
    UUID getOwnerBusinessAccountId();
    UUID getOwnerUserId();
    UUID getViewPermissionId();
    UUID getPermissionSchemaSpaceId();
    UUID getTwinflowSchemaSpaceId();
    UUID getTwinClassSchemaSpaceId();
    UUID getAliasSpaceId();
    UUID getTwinStatusId();
    String getName();
    String getDescription();
    UUID getCreatedByUserId();
    UUID getAssignerUserId();

    default String easyLog(Level level) {
        return "twin[" + getId() + "]";
    }
}
