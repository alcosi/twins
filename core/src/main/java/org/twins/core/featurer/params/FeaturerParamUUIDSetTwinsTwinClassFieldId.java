package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

@FeaturerParamType(
        id = "UUID_SET:TWINS:TWIN_CLASS_FIELD_ID",
        description = "",
        regexp = FeaturerParamUUIDSet.UUID_SET_REGEXP,
        example = FeaturerParamUUIDSet.UUID_SET_EXAMPLE,
        targetEntity = TwinClassFieldEntity.class)
public class FeaturerParamUUIDSetTwinsTwinClassFieldId extends FeaturerParamUUIDSet {
    public FeaturerParamUUIDSetTwinsTwinClassFieldId(String key) {
        super(key);
    }
}
