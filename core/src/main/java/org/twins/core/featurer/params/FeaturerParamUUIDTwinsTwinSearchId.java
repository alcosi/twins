package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.twins.core.dao.search.TwinSearchEntity;

@FeaturerParamType(
        id = "UUID:TWINS:TWIN_SEARCH_ID",
        description = "saved search id",
        regexp = FeaturerParamUUID.UUID_REGEXP,
        example = FeaturerParamUUID.UUID_EXAMPLE,
        targetEntity = TwinSearchEntity.class)
public class FeaturerParamUUIDTwinsTwinSearchId extends FeaturerParamUUID {
    public FeaturerParamUUIDTwinsTwinSearchId(String key) {
        super(key);
    }
}
