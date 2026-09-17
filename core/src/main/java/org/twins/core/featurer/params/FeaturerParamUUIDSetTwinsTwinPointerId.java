package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.dao.twin.TwinPointerEntity;

@FeaturerParamType(
        id = "UUID_SET:TWINS:TWIN_POINTER_ID",
        description = "",
        regexp = FeaturerParamUUIDSet.UUID_SET_REGEXP,
        example = FeaturerParamUUIDSet.UUID_SET_EXAMPLE,
        targetEntity = TwinPointerEntity.class)
public class FeaturerParamUUIDSetTwinsTwinPointerId extends FeaturerParamUUIDSet {
    public FeaturerParamUUIDSetTwinsTwinPointerId(String key) {
        super(key);
    }
}
