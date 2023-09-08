package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;

public abstract class FieldTyperChecks<T extends FieldValue> extends FieldTyper<T> {
    @FeaturerParam(name = "listUUID", description = "")
    public static final FeaturerParamUUID listUUID = new FeaturerParamUUID("listUUID");

    @FeaturerParam(name = "inline", description = "If true, then values will be on one row")
    public static final FeaturerParamBoolean inline = new FeaturerParamBoolean("inline");
}
