package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUID;

@FeaturerParamType(
        id = "UUID:TWINS:TWIN_CLASS_FIELD_ID",
        description = "",
        regexp = FeaturerParamUUID.UUID_REGEXP,
        example = FeaturerParamUUID.UUID_EXAMPLE)
public class FeaturerParamUUIDTwinsTwinClassFieldId extends FeaturerParamUUID {
    public FeaturerParamUUIDTwinsTwinClassFieldId(String key) {
        super(key);
    }
}
