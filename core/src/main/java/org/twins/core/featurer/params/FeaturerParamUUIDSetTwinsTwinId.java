package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.dao.twin.TwinEntity;

@FeaturerParamType(
        id = "UUID_SET:TWINS:TWIN_ID",
        description = "",
        regexp = FeaturerParamUUIDSet.UUID_SET_REGEXP,
        example = FeaturerParamUUIDSet.UUID_SET_EXAMPLE,
        targetEntity = TwinEntity.class)
public class FeaturerParamUUIDSetTwinsTwinId extends FeaturerParamUUIDSet {
    public FeaturerParamUUIDSetTwinsTwinId(String key) {
        super(key);
    }
}
