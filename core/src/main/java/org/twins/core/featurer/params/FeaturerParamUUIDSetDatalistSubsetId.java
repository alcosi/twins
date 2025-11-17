package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUIDSet;

@FeaturerParamType(
        id = "UUID_SET:TWINS:DATALIST_SUBSET_ID",
        description = "",
        regexp = FeaturerParamUUIDSet.UUID_SET_REGEXP,
        example = FeaturerParamUUIDSet.UUID_SET_EXAMPLE)
public class FeaturerParamUUIDSetDatalistSubsetId extends FeaturerParamUUIDSet {
    public FeaturerParamUUIDSetDatalistSubsetId(String key) {
        super(key);
    }
}
