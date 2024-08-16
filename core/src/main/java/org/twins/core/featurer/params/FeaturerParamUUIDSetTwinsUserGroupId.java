package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUIDSet;

@FeaturerParamType(
        id = "UUID_SET:TWINS_USER_GROUP_ID",
        description = "",
        regexp = FeaturerParamUUIDSet.UUID_SET_REGEXP,
        example = FeaturerParamUUIDSet.UUID_SET_EXAMPLE)
public class FeaturerParamUUIDSetTwinsUserGroupId extends FeaturerParamUUIDSet {
    public FeaturerParamUUIDSetTwinsUserGroupId(String key) {
        super(key);
    }
}
