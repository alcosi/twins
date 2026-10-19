package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;

public interface FieldTyperCountChildrenOfTwinClass {

    @FeaturerParam(name = "Twin class ids", description = "", order = 1)
    FeaturerParamUUIDSet twinClassIds = new FeaturerParamUUIDSetTwinsClassId("twinClassIds");

    @FeaturerParam(name = "Use extends hierarchy", description = "If true, counts twins from classes that extend the specified twin classes. If false, counts only direct twin class matches.", order = 2, optional = true, defaultValue = "false")
    FeaturerParamBoolean useExtendsHierarchy = new FeaturerParamBoolean("useExtendsHierarchy");
}
