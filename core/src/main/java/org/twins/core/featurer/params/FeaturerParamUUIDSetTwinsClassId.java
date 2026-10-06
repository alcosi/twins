package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.dao.twinclass.TwinClassEntity;

@FeaturerParamType(
        id = "UUID_SET:TWINS:TWIN_CLASS_ID",
        description = "",
        regexp = FeaturerParamUUIDSet.UUID_SET_REGEXP,
        example = FeaturerParamUUIDSet.UUID_SET_EXAMPLE,
        targetEntity = TwinClassEntity.class)
public class FeaturerParamUUIDSetTwinsClassId extends FeaturerParamUUIDSet {
    public FeaturerParamUUIDSetTwinsClassId(String key) {
        super(key);
    }
}
