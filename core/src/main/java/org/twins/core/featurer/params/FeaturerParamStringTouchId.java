package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParam;
import org.twins.core.enums.twin.Touch;

import java.util.Properties;

@FeaturerParamType(
        id = "STRING:TWINS:TWIN_TOUCH_ID",
        description = "WATCHED",
        regexp = ".*",
        example = "name")
public class FeaturerParamStringTouchId extends FeaturerParam<Touch> {
    public FeaturerParamStringTouchId(String key) {
        super(key);
    }

    @Override
    public Touch extract(Properties properties) {
        return Touch.valueOfId(properties.get(key).toString());
    }
}
