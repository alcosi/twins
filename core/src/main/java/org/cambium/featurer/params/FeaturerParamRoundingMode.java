package org.cambium.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.math.RoundingMode;
import java.util.Properties;

@FeaturerParamType(
        id = "ROUNDING_MODE",
        description = "java.math.RoundingMode",
        regexp = FeaturerParamRoundingMode.ROUNDING_MODE_REGEXP,
        example = "HALF_UP"
)
public class FeaturerParamRoundingMode extends FeaturerParam<RoundingMode> {

    public static final String ROUNDING_MODE_REGEXP = "^(UP|DOWN|CEILING|FLOOR|HALF_UP|HALF_DOWN|HALF_EVEN|UNNECESSARY)$";

    public FeaturerParamRoundingMode(String key) {
        super(key);
    }

    @Override
    public RoundingMode extract(Properties properties) {
        String value = (String) properties.get(key);
        return value == null
                ? RoundingMode.HALF_UP
                : RoundingMode.valueOf(value);
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (value == null) {
            return;
        }
        try {
            RoundingMode.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new ServiceException(
                    ErrorCodeCommon.FEATURER_WRONG_PARAMS,
                    "param[" + key + "] value[" + value + "] is not valid RoundingMode"
            );
        }
    }
}
