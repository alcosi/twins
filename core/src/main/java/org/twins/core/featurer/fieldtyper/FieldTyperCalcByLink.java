package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;

public interface FieldTyperCalcByLink {
    @FeaturerParam(name = "Link id", order = 1)
    FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "Src or dst", order = 2, optional = true, defaultValue = "true")
    FeaturerParamBoolean srcElseDst = new FeaturerParamBoolean("srcElseDst");

    @FeaturerParam(name = "Linked twin in status id set", order = 3, optional = true)
    FeaturerParamUUIDSet linkedTwinInStatusIdSet = new FeaturerParamUUIDSetTwinsStatusId("linkedTwinInStatusIdSet");

    @FeaturerParam(name = "Linked twin of class ids", order = 4, optional = true)
    FeaturerParamUUIDSet linkedTwinOfClassIds = new FeaturerParamUUIDSetTwinsClassId("linkedTwinOfClassIds");

    @FeaturerParam(name = "Status exclude", order = 5, optional = true, defaultValue = "false")
    FeaturerParamBoolean statusExclude = new FeaturerParamBoolean("statusExclude");
}
