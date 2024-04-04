package org.cambium.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;

@FeaturerParamType(
        id = "DOUBLE",
        description = "any number",
        regexp = "^-?\\d+(\\.\\d+)?$", //^-?\d+(\.\d+)?([eE][-+]?\d+)?$ - with exp. form
        example = "108.84")
public class FeaturerParamDouble extends FeaturerParam<Double> {
    public FeaturerParamDouble(String key) {
        super(key);
    }

    @Override
    public Double extract(Properties properties) {
        final String value = properties.get(key).toString();
        return value.isEmpty() ? null : Double.parseDouble(value);
    }
}
