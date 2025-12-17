package org.cambium.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;

@FeaturerParamType(
        id = "TRIBOOL",
        description = "true, false or null value",
        regexp = FeaturerParamTribool.TRIBOOL_REGEXP,
        example = "true")
public class FeaturerParamTribool extends FeaturerParam<Boolean> {
    public static final String TRIBOOL_REGEXP = "^true$|^false$|^null";
    public FeaturerParamTribool(String key) {
        super(key);
    }

    @Override
    public Boolean extract(Properties properties) {
        return Boolean.parseBoolean((String) properties.get(key));
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (!value.matches(TRIBOOL_REGEXP))
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS, "param[" + key + "] value[" + value + "] is not boolean null");
    }
}
