package org.cambium.featurer.params;


import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;
import org.twins.core.dao.twinclass.TwinClassFieldConditionElementType;

import java.util.Properties;

@FeaturerParamType(
        id = "TWIN_CONDITION_ELEMENT_TYPE",
        description = "twin condition element type",
        regexp = FeaturerParamStringTwinConditionElementType.TWIN_CONDITION_ELEMENT_TYPE_REGEXP,
        example = "value")
public class FeaturerParamStringTwinConditionElementType extends FeaturerParam<TwinClassFieldConditionElementType> {
    public static final String TWIN_CONDITION_ELEMENT_TYPE_REGEXP = "value|param";

    public FeaturerParamStringTwinConditionElementType(String key) {
        super(key);
    }

    @Override
    public TwinClassFieldConditionElementType extract(Properties properties) {
        String value = (String) properties.get(key);
        return value != null ?
                TwinClassFieldConditionElementType.valueOf(value.toLowerCase()) :
                TwinClassFieldConditionElementType.value;
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (value == null || !value.matches(TWIN_CONDITION_ELEMENT_TYPE_REGEXP)) {
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS,
                    "param[" + key + "] value[" + value + "] must be one of: " + TWIN_CONDITION_ELEMENT_TYPE_REGEXP);
        }
    }
}
