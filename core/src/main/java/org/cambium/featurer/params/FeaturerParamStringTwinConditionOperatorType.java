package org.cambium.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;
import org.twins.core.enums.twinclass.TwinClassFieldConditionOperator;

import java.util.Properties;

@FeaturerParamType(
        id = "TWIN_CONDITION_OPERATOR_TYPE",
        description = "twin condition operator type",
        regexp = FeaturerParamStringTwinConditionOperatorType.TWIN_CONDITION_OPERATOR_TYPE_REGEXP,
        example = "eq")
public class FeaturerParamStringTwinConditionOperatorType extends FeaturerParam<TwinClassFieldConditionOperator> {
    public static final String TWIN_CONDITION_OPERATOR_TYPE_REGEXP = "eq|neq|lt|gt|contains|in";

    public FeaturerParamStringTwinConditionOperatorType(String key) {
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
