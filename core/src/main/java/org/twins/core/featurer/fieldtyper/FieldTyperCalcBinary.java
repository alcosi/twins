package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

public interface FieldTyperCalcBinary {
    @FeaturerParam(name = "firstFieldId", description = "First field id", order = 1)
    FeaturerParamUUID firstFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("firstFieldId");

    @FeaturerParam(name = "secondFieldId", description = "Second field id", order = 2)
    FeaturerParamUUID secondFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("secondFieldId");

    @FeaturerParam(name = "Decimal places", description = "Number of decimal places.", order = 3)
    FeaturerParamInt decimalPlaces = new FeaturerParamInt("decimalPlaces");

    @FeaturerParam(name = "Round", description = "Round a number to the required number of decimal places", order = 4, optional = true, defaultValue = "true")
    FeaturerParamBoolean round = new FeaturerParamBoolean("round");
}
