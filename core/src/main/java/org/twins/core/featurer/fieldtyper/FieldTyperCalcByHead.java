package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;

public interface FieldTyperCalcByHead {
    @FeaturerParam(name = "Children twin in status ids", order = 1, optional = true)
    FeaturerParamUUIDSet childrenTwinInStatusIds = new FeaturerParamUUIDSetTwinsStatusId("childrenTwinInStatusIds");

    @FeaturerParam(name = "Children twin of class ids", order = 2, optional = true)
    FeaturerParamUUIDSet childrenTwinOfClassIds = new FeaturerParamUUIDSetTwinsClassId("childrenTwinOfClassIds");

    @FeaturerParam(name = "Status exclude", order = 3, optional = true, defaultValue = "false")
    FeaturerParamBoolean statusExclude = new FeaturerParamBoolean("statusExclude");
}

