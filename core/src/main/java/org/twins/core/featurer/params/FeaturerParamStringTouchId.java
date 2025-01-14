package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParam;
import org.twins.core.dao.history.HistoryType;
import org.twins.core.dao.twin.TwinTouchEntity;

import java.util.Arrays;
import java.util.Properties;

@FeaturerParamType(
        id = "STRING:TWINS:TWIN_TOUCH_ID",
        description = "WATCHED",
        regexp = ".*",
        example = "name")
public class FeaturerParamStringTouchId extends FeaturerParam<TwinTouchEntity.Touch> {
    public FeaturerParamStringTouchId(String key) {
        super(key);
    }

    @Override
    public TwinTouchEntity.Touch extract(Properties properties) {
        return TwinTouchEntity.Touch.valueOfId(properties.get(key).toString());
    }
}
