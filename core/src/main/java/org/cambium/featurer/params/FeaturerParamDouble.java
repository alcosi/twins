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
        Object raw = properties.get(key);
        if (raw == null) return null;
        String value = raw.toString();
        return value.isEmpty() ? null : Double.parseDouble(value);
    }
}
