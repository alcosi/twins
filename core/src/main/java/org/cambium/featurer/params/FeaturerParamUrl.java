
package org.cambium.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;

@FeaturerParamType(
        id = "URL",
        description = "url formatted string",
        regexp = FeaturerParamUrl.URL_REGEXP,
        example = "https://example.com")
public class FeaturerParamUrl extends FeaturerParam<String> {
    public static final String URL_REGEXP = "^https?:\\/\\/(?:www\\.)?[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)+(?::\\d{1,5})?(?:\\/[^\\s]*)?(?:\\?[^\\s]*)?$";

    public FeaturerParamUrl(String key) {
        super(key);
    }

    @Override
    public String extract(Properties properties) {
        return (String) properties.get(key);
    }
}
