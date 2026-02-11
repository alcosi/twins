package org.twins.core.featurer.fieldinitializer;

import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;

public interface FieldInitializerThrowIfNull {
    @FeaturerParam(name = "Throw If Null", optional = true, defaultValue = "false", description = "", order = 1)
    FeaturerParamBoolean throwIfNull = new FeaturerParamBoolean("throwIfNull");
}
