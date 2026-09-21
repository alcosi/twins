package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.twins.core.dao.link.LinkEntity;

@FeaturerParamType(
        id = "UUID:TWINS:LINK_ID",
        description = "",
        regexp = FeaturerParamUUID.UUID_REGEXP,
        example = FeaturerParamUUID.UUID_EXAMPLE,
        targetEntity = LinkEntity.class)
public class FeaturerParamUUIDTwinsLinkId extends FeaturerParamUUID {
    public FeaturerParamUUIDTwinsLinkId(String key) {
        super(key);
    }
}
