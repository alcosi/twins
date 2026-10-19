package org.cambium.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.math.BigDecimal;
import java.util.Properties;

@FeaturerParamType(
        id = "BIGDECIMAL",
        description = "any decimal number",
        regexp = "^-?\\d+(\\.\\d+)?$",
        example = "108.84")
public class FeaturerParamBigDecimal extends FeaturerParam<BigDecimal> {
    public static final String BIGDECIMAL_REGEXP = "^-?\\d+(\\.\\d+)?$";

    public FeaturerParamBigDecimal(String key) {
        super(key);
    }

    @Override
    public BigDecimal extract(Properties properties) {
        String value = properties.getProperty(key);
        return StringUtils.isEmpty(value) ? null : new BigDecimal(value);
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        try {
            new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS, "param[" + key + "] value[" + value + "] is not valid BigDecimal");
        }
    }
}
