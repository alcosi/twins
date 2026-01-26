package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParam;
import org.twins.core.enums.twinclass.FieldCheckboxType;

import java.util.Properties;

@FeaturerParamType(
        id = "STRING:TWINS:CHECKBOX_TYPE",
        description = "",
        regexp = ".*",
        example = "name")
public class FeaturerParamStringTwinsCheckboxType extends FeaturerParam<FieldCheckboxType> {
    public FeaturerParamStringTwinsCheckboxType(String key) {super(key);}

    @Override
    public FieldCheckboxType extract(Properties properties) {
        return FieldCheckboxType.valueOf(properties.get(key).toString());
    }

}
