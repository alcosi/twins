package org.cambium.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;

@FeaturerParamType(
        id = "URL",
        description = "url formatted string",
        regexp = ".*", //todo
        example = "https://example.com")
public class FeaturerParamUrl extends FeaturerParam<String> {
    public FeaturerParamUrl(String key) {
        super(key);
    }

    @Override
    public String extract(Properties properties) {
        return (String) properties.get(key);
    }
}
