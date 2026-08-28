package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.math.BigDecimal;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1337, name = "Division", description = "First / Second")
public class FieldTyperCalcDivision extends FieldTyperCalcBinaryBase {

    @FeaturerParam(name = "divisionByZeroResult", description = "Result if division by zero", defaultValue = "<n/a>")
    public static final FeaturerParamString divisionByZeroResult = new FeaturerParamString("divisionByZeroResult");

    @Override
    protected String calculate(BigDecimal v1, BigDecimal v2, Properties properties) throws ServiceException {
        var d1 = v1 != null ? v1 : BigDecimal.ZERO;
        var d2 = v2 != null ? v2 : BigDecimal.ZERO;

        if (d2.compareTo(BigDecimal.ZERO) == 0) {
            return divisionByZeroResult.extract(properties);
        }

        var scale = decimalPlaces.extract(properties);

        var result = scaleAndRound(
                d1.divide(d2, scale, roundingMode.extract(properties)),
                properties
        );

        return result.toPlainString();
    }
}
