package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.dao.link.LinkEntity;

@FeaturerParamType(
        id = "UUID_SET:TWINS:LINK_ID",
        description = "",
        regexp = FeaturerParamUUIDSet.UUID_SET_REGEXP,
        example = FeaturerParamUUIDSet.UUID_SET_EXAMPLE,
        targetEntity = LinkEntity.class)
public class FeaturerParamUUIDSetTwinsLinkId extends FeaturerParamUUIDSet {
    public FeaturerParamUUIDSetTwinsLinkId(String key) {
        super(key);
    }
}
