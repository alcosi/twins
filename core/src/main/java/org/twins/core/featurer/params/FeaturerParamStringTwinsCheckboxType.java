package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParam;
import org.twins.core.domain.enum_.twin.CheckboxType;

import java.util.Properties;

@FeaturerParamType(
        id = "STRING:TWINS:CHECKBOX_TYPE",
        description = "",
        regexp = ".*",
        example = "name")
public class FeaturerParamStringTwinsCheckboxType extends FeaturerParam<CheckboxType> {
    public FeaturerParamStringTwinsCheckboxType(String key) {super(key);}

    @Override
    public CheckboxType extract(Properties properties) {
        return CheckboxType.valueOf(properties.get(key).toString());
    }

}
