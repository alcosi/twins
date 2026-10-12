package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.twins.core.dao.space.SpaceRoleEntity;

@FeaturerParamType(
        id = "UUID:TWINS:SPACE_ROLE_ID",
        description = "",
        regexp = FeaturerParamUUID.UUID_REGEXP,
        example = FeaturerParamUUID.UUID_EXAMPLE,
        targetEntity = SpaceRoleEntity.class)
public class FeaturerParamUUIDTwinsSpaceRoleId extends FeaturerParamUUID {
    public FeaturerParamUUIDTwinsSpaceRoleId(String key) {
        super(key);
    }
}
