package org.cambium.featurer.params;


import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;

@FeaturerParamType(
        id = "PHONE",
        description = "phone number in international format +CCNNNNNNNNNN",
        regexp = FeaturerParamPhone.PHONE_REGEXP,
        example = "+42934563345")
public class FeaturerParamPhone extends FeaturerParam<String> {
    public static final String PHONE_REGEXP = "^(\\+\\d{7,15})$";
    public FeaturerParamPhone(String key) {
        super(key);
    }

    @Override
    public String extract(Properties properties) {
        return (String) properties.get(key);
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (value == null || !value.matches(PHONE_REGEXP))
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS, "param[" + key + "] value[" + value + "] is not valid phone");
    }
}
