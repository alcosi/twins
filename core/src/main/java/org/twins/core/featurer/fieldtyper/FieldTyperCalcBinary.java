package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

public interface FieldTyperCalcBinary extends FieldTyperScalable {
    @FeaturerParam(name = "firstFieldId", description = "First field id", order = 1)
    FeaturerParamUUID firstFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("firstFieldId");

    @FeaturerParam(name = "secondFieldId", description = "Second field id", order = 2)
    FeaturerParamUUID secondFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("secondFieldId");
}
