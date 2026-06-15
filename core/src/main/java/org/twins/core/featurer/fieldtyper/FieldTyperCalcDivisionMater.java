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
@Featurer(
        id = FeaturerTwins.ID_1356,
        name = "Division (saved)",
        description = "Save first divided by second field on serializeValue, and return saved total from database"
)
public class FieldTyperCalcDivisionMater extends FieldTyperCalcBinaryMater {

    @FeaturerParam(name = "divisionByZeroResul", description = "Result if division by zero", defaultValue = "<n/a>")
    public static final FeaturerParamString divisionByZeroResul = new FeaturerParamString("divisionByZeroResul");

    @Override
    protected BigDecimal calculate(BigDecimal v1, BigDecimal v2, Properties properties) throws ServiceException {
        var d1 = v1 != null ? v1 : BigDecimal.ZERO;
        var d2 = v2 != null ? v2 : BigDecimal.ZERO;

        if (d2.compareTo(BigDecimal.ZERO) == 0) {
            return divisionByZeroResult(properties);
        }

        Integer scaleParam = FieldTyperScalable.decimalPlaces.extract(properties);
        int scale = scaleParam == null ? 10 : scaleParam;
        RoundingMode roundingModeValue = FieldTyperScalable.roundingMode.extract(properties) == null
                ? RoundingMode.HALF_UP
                : FieldTyperScalable.roundingMode.extract(properties);

        return scaleAndRound(d1.divide(d2, scale, roundingModeValue), properties);
    }

    private BigDecimal divisionByZeroResult(Properties properties) throws ServiceException {
        var divisionByZeroResult = divisionByZeroResul.extract(properties);
        try {
            return scaleAndRound(new BigDecimal(divisionByZeroResult), properties);
        } catch (NumberFormatException e) {
            return scaleAndRound(BigDecimal.ZERO, properties);
        }
    }
}
