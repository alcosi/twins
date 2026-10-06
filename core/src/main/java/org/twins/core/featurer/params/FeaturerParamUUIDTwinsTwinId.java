package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.twins.core.dao.twin.TwinEntity;

@FeaturerParamType(
        id = "UUID:TWINS:TWIN_ID",
        description = "",
        regexp = FeaturerParamUUID.UUID_REGEXP,
        example = FeaturerParamUUID.UUID_EXAMPLE,
        targetEntity = TwinEntity.class)
public class FeaturerParamUUIDTwinsTwinId extends FeaturerParamUUID {
    public FeaturerParamUUIDTwinsTwinId(String key) {
        super(key);
    }
}
