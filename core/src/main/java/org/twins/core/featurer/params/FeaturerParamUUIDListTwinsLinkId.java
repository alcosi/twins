package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUIDList;

@FeaturerParamType(
        id = "UUID_LIST:TWINS:LINK_ID",
        description = "",
        regexp = FeaturerParamUUIDList.UUID_LIST_REGEXP,
        example = FeaturerParamUUIDList.UUID_LIST_EXAMPLE)
public class FeaturerParamUUIDListTwinsLinkId extends FeaturerParamUUIDList {
    public FeaturerParamUUIDListTwinsLinkId(String key) {
        super(key);
    }
}
