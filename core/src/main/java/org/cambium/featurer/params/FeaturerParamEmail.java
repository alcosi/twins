package org.cambium.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;

@FeaturerParamType(
        id = "EMAIL",
        description = "email",
        regexp = "^(.+)@(.+)$",
        example = "john_doe@mail.biz")
public class FeaturerParamEmail extends FeaturerParam<String> {
    public FeaturerParamEmail(String key) {
        super(key);
    }

    @Override
    public String extract(Properties properties) {
        return (String) properties.get(key);
    }
}
