package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1339, name = "Subtraction", description = "First - Second")
public class FieldTyperCalcSubtraction extends FieldTyperCalcBinaryBase {

    @Override
    protected String calculate(BigDecimal v1, BigDecimal v2, Properties properties) throws ServiceException {
        BigDecimal d1 = v1 != null ? v1 : BigDecimal.ZERO;
        BigDecimal d2 = v2 != null ? v2 : BigDecimal.ZERO;

        BigDecimal result = d1.subtract(d2);

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
