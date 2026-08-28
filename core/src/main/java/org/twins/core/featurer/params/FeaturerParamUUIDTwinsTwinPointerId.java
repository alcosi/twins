package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUID;

@FeaturerParamType(
        id = "UUID:TWINS:TWIN_POINTER_ID",
        description = "",
        regexp = FeaturerParamUUID.UUID_REGEXP,
        example = FeaturerParamUUID.UUID_EXAMPLE)
public class FeaturerParamUUIDTwinsTwinPointerId extends FeaturerParamUUID {
    public FeaturerParamUUIDTwinsTwinPointerId(String key) {
        super(key);
    }
}
