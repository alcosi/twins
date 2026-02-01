package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1338, name = "Multiplication", description = "First * Second")
public class FieldTyperCalcMultiplication extends FieldTyperCalcBinaryBase {

    @FeaturerParam(name = "replaceZeroWithOne", description = "if some filed value is null or 0, then mulitply on 1")
    public static final FeaturerParamBoolean replaceZeroWithOne = new FeaturerParamBoolean("replaceZeroWithOne");

    @Override
    protected String calculate(BigDecimal v1, BigDecimal v2, Properties properties) throws ServiceException {
        boolean replace = replaceZeroWithOne.extract(properties);
        Integer scale = decimalPlaces.extract(properties);
        RoundingMode roundingModeParam = roundingMode.extract(properties);

        BigDecimal d1 = prepare(v1, replace);
        BigDecimal d2 = prepare(v2, replace);

        BigDecimal result = d1.multiply(d2);

        if (scale != null) {
            result = result.setScale(scale, roundingModeParam);
            result = result.stripTrailingZeros();
        }

        return result.toPlainString();
    }

    private BigDecimal prepare(BigDecimal v, boolean replace) {
        if (v == null) {
            return BigDecimal.ZERO;
        }
        if (replace && v.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }
        return v;
    }
}
