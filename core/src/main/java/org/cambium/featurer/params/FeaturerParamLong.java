package org.cambium.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;

@FeaturerParamType(
        id = "LONG",
        description = "any long number",
        regexp = FeaturerParamLong.LONG_REGEXP,
        example = "108")
public class FeaturerParamLong extends FeaturerParam<Long> {
    public static final String LONG_REGEXP = "^-?\\d+$";
    public FeaturerParamLong(String key) {
        super(key);
    }

    @Override
    public Long extract(Properties properties) {
        final String value = properties.get(key).toString();
        return value.isEmpty() ? null : Long.parseLong(value);
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (value == null || !value.matches(LONG_REGEXP))
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS, "param[" + key + "] value[" + value + "] is not valid long");
    }
}
