package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUIDSet;

@FeaturerParamType(
        id = "UUID_SET:TWINS:TWIN_CLASS_FREEZE_ID",
        description = "",
        regexp = FeaturerParamUUIDSet.UUID_SET_REGEXP,
        example = FeaturerParamUUIDSet.UUID_SET_EXAMPLE)
public class FeaturerParamUUIDSetTwinClassFreezeId extends FeaturerParamUUIDSet {
    public FeaturerParamUUIDSetTwinClassFreezeId(String key) {
        super(key);
    }
}
