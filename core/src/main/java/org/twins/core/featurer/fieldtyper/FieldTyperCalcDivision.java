package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1337, name = "Division", description = "First / Second")
public class FieldTyperCalcDivision extends FieldTyperCalcBinaryBase {

    @FeaturerParam(name = "divisionByZeroResul", description = "Result if division by zero", defaultValue = "<n/a>")
    public static final FeaturerParamString divisionByZeroResul = new FeaturerParamString("divisionByZeroResul");

    @Override
    protected String calculate(BigDecimal v1, BigDecimal v2, Properties properties) throws ServiceException {
        BigDecimal d1 = v1 != null ? v1 : BigDecimal.ZERO;
        BigDecimal d2 = v2 != null ? v2 : BigDecimal.ZERO;

        if (d2.compareTo(BigDecimal.ZERO) == 0) {
            return divisionByZeroResul.extract(properties);
        }

        BigDecimal result = d1.divide(d2, 10, RoundingMode.HALF_UP);

        // Apply rounding if parameters are specified
        Integer scale = decimalPlaces.extract(properties);
        RoundingMode roundingModeParam = roundingMode.extract(properties);

        if (scale != null) {
            result = result.setScale(scale, roundingModeParam);
            result = result.stripTrailingZeros();
        }

        return result.toPlainString();
    }
}
