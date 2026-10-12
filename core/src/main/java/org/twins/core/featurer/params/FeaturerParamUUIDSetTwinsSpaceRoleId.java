package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.dao.space.SpaceRoleEntity;

@FeaturerParamType(
        id = "UUID_SET:TWINS:SPACE_ROLE_ID",
        description = "",
        regexp = FeaturerParamUUIDSet.UUID_SET_REGEXP,
        example = FeaturerParamUUIDSet.UUID_SET_EXAMPLE,
        targetEntity = SpaceRoleEntity.class)
public class FeaturerParamUUIDSetTwinsSpaceRoleId extends FeaturerParamUUIDSet {
    public FeaturerParamUUIDSetTwinsSpaceRoleId(String key) {
        super(key);
    }
}
