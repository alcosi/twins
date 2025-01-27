package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUID;

@FeaturerParamType(
        id = "UUID:TWINS:TWINFLOW_SCHEMA_ID",
        description = "",
        regexp = FeaturerParamUUID.UUID_REGEXP,
        example = FeaturerParamUUID.UUID_EXAMPLE)
public class FeaturerParamUUIDTwinsTwinflowSchemaId extends FeaturerParamUUID {
    public FeaturerParamUUIDTwinsTwinflowSchemaId(String key) {
        super(key);
    }
}
