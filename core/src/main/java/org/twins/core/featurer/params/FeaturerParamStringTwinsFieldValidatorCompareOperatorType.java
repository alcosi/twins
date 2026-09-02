package org.twins.core.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParam;
import org.twins.core.enums.twinclass.FieldValidatorCompareOperator;

import java.util.Properties;

@FeaturerParamType(
        id = "STRING:TWINS:FIELD_VALIDATOR_COMPARE_OPERATOR_TYPE",
        description = "field validator compare operator type",
        regexp = FeaturerParamStringTwinsFieldValidatorCompareOperatorType.FIELD_VALIDATOR_COMPARE_OPERATOR_TYPE_REGEXP,
        example = "ge"
)
public class FeaturerParamStringTwinsFieldValidatorCompareOperatorType extends FeaturerParam<FieldValidatorCompareOperator> {
    public static final String FIELD_VALIDATOR_COMPARE_OPERATOR_TYPE_REGEXP = "ge|gt|le|lt|eq";

    public FeaturerParamStringTwinsFieldValidatorCompareOperatorType(String key) {
        super(key);
    }

    @Override
    public FieldValidatorCompareOperator extract(Properties properties) {
        String value = (String) properties.get(key);
        return value != null ?
                FieldValidatorCompareOperator.valueOf(value.toLowerCase()) :
                FieldValidatorCompareOperator.ge;
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (value == null || !value.matches(FIELD_VALIDATOR_COMPARE_OPERATOR_TYPE_REGEXP)) {
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS,
                    "param[" + key + "] value[" + value + "] must be one of: " + FIELD_VALIDATOR_COMPARE_OPERATOR_TYPE_REGEXP);
        }
    }
}
