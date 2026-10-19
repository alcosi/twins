package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.twins.core.dao.permission.PermissionSchemaEntity;

@FeaturerParamType(
        id = "UUID:TWINS:PERMISSION_SCHEMA_ID",
        description = "",
        regexp = FeaturerParamUUID.UUID_REGEXP,
        example = FeaturerParamUUID.UUID_EXAMPLE,
        targetEntity = PermissionSchemaEntity.class)
public class FeaturerParamUUIDTwinsPermissionSchemaId extends FeaturerParamUUID {
    public FeaturerParamUUIDTwinsPermissionSchemaId(String key) {
        super(key);
    }
}
