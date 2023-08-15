package org.cambium.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;

@FeaturerParamType(
        id = "BOOLEAN",
        description = "true or false value",
        regexp = "^true$|^false$",
        example = "true")
public class FeaturerParamBoolean extends FeaturerParam<Boolean> {
    public FeaturerParamBoolean(String key) {
        super(key);
    }

    @Override
    public Boolean extract(Properties properties) {
        return Boolean.parseBoolean((String) properties.get(key));
    }
}
