package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUIDList;
import org.twins.core.dao.search.TwinSearchEntity;

@FeaturerParamType(
        id = "UUID_LIST:TWINS:TWIN_SEARCH_ID",
        description = "ordered list of saved search ids",
        regexp = FeaturerParamUUIDList.UUID_LIST_REGEXP,
        example = FeaturerParamUUIDList.UUID_LIST_EXAMPLE,
        targetEntity = TwinSearchEntity.class)
public class FeaturerParamUUIDListTwinsTwinSearchId extends FeaturerParamUUIDList {
    public FeaturerParamUUIDListTwinsTwinSearchId(String key) {
        super(key);
    }
}
