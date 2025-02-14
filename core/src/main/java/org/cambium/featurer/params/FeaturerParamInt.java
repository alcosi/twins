package org.cambium.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;

@FeaturerParamType(
        id = "INT",
        description = "any integer number",
        regexp = FeaturerParamInt.INTEGER_REGEXP,
        example = "108")
public class FeaturerParamInt extends FeaturerParam<Integer> {
    public static final String INTEGER_REGEXP = "^-?\\d+$";
    public FeaturerParamInt(String key) {
        super(key);
    }

    @Override
    public Integer extract(Properties properties) {
        final String value = properties.get(key).toString();
        return value.isEmpty() ? null : Integer.parseInt(value);
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (value == null || !value.matches(INTEGER_REGEXP))
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS, "param[" + key + "] value[" + value + "] is not valid integer");
    }
}
