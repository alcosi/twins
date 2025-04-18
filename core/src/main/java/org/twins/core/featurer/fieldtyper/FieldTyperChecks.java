package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorChecks;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsDataListId;

public abstract class FieldTyperChecks<D extends  FieldDescriptorChecks, T extends FieldValue, A extends TwinFieldSearchNotImplemented> extends FieldTyperSimple<D, T, A> {
    @FeaturerParam(name = "Datalist", description = "")
    public static final FeaturerParamUUID listUUID = new FeaturerParamUUIDTwinsDataListId("listUUID");

    @FeaturerParam(name = "Inline", description = "If true, then values will be on one row")
    public static final FeaturerParamBoolean inline = new FeaturerParamBoolean("inline");
}
