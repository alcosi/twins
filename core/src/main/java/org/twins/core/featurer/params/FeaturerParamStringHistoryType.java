package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParam;
import org.twins.core.enums.history.HistoryType;

import java.util.Properties;

@FeaturerParamType(
        id = "STRING:TWINS:HISTORY_TYPE",
        description = "",
        regexp = ".*",
        example = "name")
public class FeaturerParamStringHistoryType extends FeaturerParam<HistoryType> {
    public FeaturerParamStringHistoryType(String key) {super(key);}

    @Override
    public HistoryType extract(Properties properties) {
        return HistoryType.valueOf(properties.get(key).toString());
    }
}
