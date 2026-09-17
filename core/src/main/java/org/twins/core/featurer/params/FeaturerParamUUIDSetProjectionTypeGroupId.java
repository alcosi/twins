package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;

@FeaturerParamType(
        id = "UUID_SET:TWINS:PROJECTION_TYPE_GROUP_ID",
        description = "",
        regexp = FeaturerParamUUIDSet.UUID_SET_REGEXP,
        example = FeaturerParamUUIDSet.UUID_SET_EXAMPLE,
        targetEntity = ProjectionTypeGroupEntity.class)
public class FeaturerParamUUIDSetProjectionTypeGroupId extends FeaturerParamUUIDSet {
    public FeaturerParamUUIDSetProjectionTypeGroupId(String key) {
        super(key);
    }
}
