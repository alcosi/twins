package org.cambium.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;

@FeaturerParamType(
        id = "INT",
        description = "any integer number",
        regexp = "^-?\\d+$",
        example = "108")
public class FeaturerParamInt extends FeaturerParam<Integer> {
    public FeaturerParamInt(String key) {
        super(key);
    }

    @Override
    public Integer extract(Properties properties) {
        return Integer.parseInt(properties.get(key).toString());
    }
}
