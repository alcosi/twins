package org.cambium.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;

@FeaturerParamType(
        id = "STRING",
        description = "any string",
        regexp = ".*",
        example = "Hello world!")
public class FeaturerParamString extends FeaturerParam<String> {
    public FeaturerParamString(String key) {
        super(key);
    }

    @Override
    public String extract(Properties properties) {
        return (String) properties.get(key);
    }
}
