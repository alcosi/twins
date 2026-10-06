package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.dao.datalist.DataListOptionEntity;

@FeaturerParamType(
        id = "UUID_SET:TWINS:DATALIST_OPTION_ID",
        description = "",
        regexp = FeaturerParamUUIDSet.UUID_SET_REGEXP,
        example = FeaturerParamUUIDSet.UUID_SET_EXAMPLE,
        targetEntity = DataListOptionEntity.class)
public class FeaturerParamUUIDSetDatalistOptionId extends FeaturerParamUUIDSet {
    public FeaturerParamUUIDSetDatalistOptionId(String key) {
        super(key);
    }
}
