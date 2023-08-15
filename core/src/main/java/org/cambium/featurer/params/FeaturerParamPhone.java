package org.cambium.featurer.params;


import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;

@FeaturerParamType(
        id = "PHONE",
        description = "phone number in international format +CCNNNNNNNNNN",
        regexp = "^(\\+\\d{7,15})$",
        example = "+42934563345")
public class FeaturerParamPhone extends FeaturerParam<String> {
    public FeaturerParamPhone(String key) {
        super(key);
    }

    @Override
    public String extract(Properties properties) {
        return (String) properties.get(key);
    }
}
