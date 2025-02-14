package org.cambium.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;

@FeaturerParamType(
        id = "BOOLEAN",
        description = "true or false value",
        regexp = FeaturerParamBoolean.BOOLEAN_REGEXP,
        example = "true")
public class FeaturerParamBoolean extends FeaturerParam<Boolean> {
    public static final String BOOLEAN_REGEXP = "^true$|^false$";
    public FeaturerParamBoolean(String key) {
        super(key);
    }

    @Override
    public Boolean extract(Properties properties) {
        return Boolean.parseBoolean((String) properties.get(key));
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (value == null || !value.matches(BOOLEAN_REGEXP))
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS, "param[" + key + "] value[" + value + "] is not boolean");
    }
}
