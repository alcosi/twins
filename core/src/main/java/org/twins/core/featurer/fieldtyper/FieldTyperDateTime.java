package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;

import java.util.Properties;

public interface FieldTyperDateTime {
    @FeaturerParam(name = "Pattern", description = "pattern for timestamp value", optional = true, defaultValue = "yyyy-MM-dd'T'HH:mm:ss")
    FeaturerParamString pattern = new FeaturerParamString("pattern");

    default String getPattern(Properties properties) {
        return pattern.extract(properties);
    }
}

