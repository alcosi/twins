package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParam;
import org.twins.core.domain.TwinBasicFields;

import java.util.Properties;

@FeaturerParamType(
        id = "STRING:TWINS_TWIN_BASIC_FIELD",
        description = "",
        regexp = ".*",
        example = "name")
public class FeaturerParamBasicsTwinBasicField extends FeaturerParam<TwinBasicFields.Basics> {
    public FeaturerParamBasicsTwinBasicField(String key) {
        super(key);
    }

    @Override
    public TwinBasicFields.Basics extract(Properties properties) {
        TwinBasicFields.Basics ret = TwinBasicFields.Basics.valueOf(properties.get(key).toString());
        return ret;
    }
}
