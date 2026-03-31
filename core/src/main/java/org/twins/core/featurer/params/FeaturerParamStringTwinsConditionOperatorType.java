package org.twins.core.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParam;
import org.twins.core.enums.twinclass.TwinClassFieldConditionOperator;

import java.util.Properties;

@FeaturerParamType(
        id = "STRING:TWINS:TWIN_CONDITION_OPERATOR_TYPE",
        description = "twin condition operator type",
        regexp = FeaturerParamStringTwinsConditionOperatorType.TWIN_CONDITION_OPERATOR_TYPE_REGEXP,
        example = "eq")
public class FeaturerParamStringTwinsConditionOperatorType extends FeaturerParam<TwinClassFieldConditionOperator> {
    public static final String TWIN_CONDITION_OPERATOR_TYPE_REGEXP = "eq|neq|lt|gt|contains|in";

    public FeaturerParamStringTwinsConditionOperatorType(String key) {
        super(key);
    }

    @Override
    public TwinClassFieldConditionOperator extract(Properties properties) {
        String value = (String) properties.get(key);
        return value != null ?
                TwinClassFieldConditionOperator.valueOf(value.toLowerCase()) :
                TwinClassFieldConditionOperator.eq;
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (value == null || !value.matches(TWIN_CONDITION_OPERATOR_TYPE_REGEXP)) {
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS,
                    "param[" + key + "] value[" + value + "] must be one of: " + TWIN_CONDITION_OPERATOR_TYPE_REGEXP);
        }
    }
}
