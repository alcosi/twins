package org.cambium.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;

@FeaturerParamType(
        id = "EMAIL",
        description = "email",
        regexp = FeaturerParamEmail.EMAIL_REGEXP,
        example = "john_doe@mail.biz")
public class FeaturerParamEmail extends FeaturerParam<String> {
    public static final String EMAIL_REGEXP = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";;

    public FeaturerParamEmail(String key) {
        super(key);
    }

    @Override
    public String extract(Properties properties) {
        return (String) properties.get(key);
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (value == null || !value.matches(EMAIL_REGEXP))
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS, "param[" + key + "] value[" + value + "] is not a valid email");
    }
}
