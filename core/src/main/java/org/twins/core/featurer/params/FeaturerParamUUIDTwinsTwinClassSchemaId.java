package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUID;

@FeaturerParamType(
        id = "UUID:TWINS_TWIN_CLASS_SCHEMA_ID",
        description = "",
        regexp = FeaturerParamUUID.UUID_REGEXP,
        example = FeaturerParamUUID.UUID_EXAMPLE)
public class FeaturerParamUUIDTwinsTwinClassSchemaId extends FeaturerParamUUID {
    public FeaturerParamUUIDTwinsTwinClassSchemaId(String key) {
        super(key);
    }
}
