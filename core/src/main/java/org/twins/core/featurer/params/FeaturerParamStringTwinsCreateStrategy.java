package org.twins.core.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParam;
import org.twins.core.enums.twin.TwinCreateStrategy;

import java.util.Properties;

@FeaturerParamType(
        id = "STRING:TWINS:TWIN_CONDITION_OPERATOR_TYPE",
        description = "twin condition operator type",
        regexp = FeaturerParamStringTwinsCreateStrategy.TWIN_CREATE_STRATEGY_REGEXP,
        example = "eq")
public class FeaturerParamStringTwinsCreateStrategy extends FeaturerParam<TwinCreateStrategy> {
    public static final String TWIN_CREATE_STRATEGY_REGEXP = "SKETCH|STRICT|AUTO";

    public FeaturerParamStringTwinsCreateStrategy(String key) {
        super(key);
    }

    @Override
    public TwinCreateStrategy extract(Properties properties) {
        String value = (String) properties.get(key);
        return value != null ?
                TwinCreateStrategy.valueOf(value.toLowerCase()) :
                TwinCreateStrategy.STRICT;
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (value == null || !value.matches(TWIN_CREATE_STRATEGY_REGEXP)) {
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS,
                    "param[" + key + "] value[" + value + "] must be one of: " + TWIN_CREATE_STRATEGY_REGEXP);
        }
    }
}
