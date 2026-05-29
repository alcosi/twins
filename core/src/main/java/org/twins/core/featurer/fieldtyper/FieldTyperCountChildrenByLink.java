package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;

public interface FieldTyperCountChildrenByLink {

    @FeaturerParam(name = "Link ids", description = "Link ids")
    FeaturerParamUUIDSet linkIds = new FeaturerParamUUIDSetTwinsLinkId("linkIds");

    @FeaturerParam(name = "Linked twin status ids", description = "Linked twin.Status.IDs")
    FeaturerParamUUIDSet linkedTwinStatusIdList = new FeaturerParamUUIDSetTwinsStatusId("linkedTwinStatusIdList");

    @FeaturerParam(name = "Exclude", description = "Exclude(true)/Include(false) linked twin.Status.IDs from query result")
    FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");
}
