package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParam;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;

import java.util.Properties;

@FeaturerParamType(
        id = "STRING:TWINS:CHECKBOX_TYPE",
        description = "",
        regexp = ".*",
        example = "name")
public class FeaturerParamStringTwinsCheckboxType extends FeaturerParam<TwinFieldBooleanEntity.CheckboxType> {
    public FeaturerParamStringTwinsCheckboxType(String key) {super(key);}

    @Override
    public TwinFieldBooleanEntity.CheckboxType extract(Properties properties) {
        return TwinFieldBooleanEntity.CheckboxType.valueOf(properties.get(key).toString());
    }

}
