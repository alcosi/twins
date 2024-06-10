package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUID;

@FeaturerParamType(
        id = "UUID:TWINS_DATA_LIST_ID",
        description = "",
        regexp = FeaturerParamUUID.UUID_REGEXP,
        example = FeaturerParamUUID.UUID_EXAMPLE)
public class FeaturerParamUUIDTwinsDataListId extends FeaturerParamUUID {
    public FeaturerParamUUIDTwinsDataListId(String key) {
        super(key);
    }
}
