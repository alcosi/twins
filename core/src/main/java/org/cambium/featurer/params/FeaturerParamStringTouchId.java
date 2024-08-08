package org.cambium.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.twins.core.dao.twin.TwinTouchEntity;

import java.util.Properties;

@FeaturerParamType(
        id = "STRING:TOUCH_ID",
        description = "",
        regexp = ".*",
        example = "name")
public class FeaturerParamStringTouchId extends FeaturerParam<TwinTouchEntity.Touch> {
    public FeaturerParamStringTouchId(String key) {
        super(key);
    }

    @Override
    public TwinTouchEntity.Touch extract(Properties properties) {
        return TwinTouchEntity.Touch.valueOf(properties.get(key).toString());
    }
}
