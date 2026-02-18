package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamRoundingMode;

import java.math.BigDecimal;
import java.util.Properties;

public interface FieldTyperScalable {

    @FeaturerParam(name = "Decimal places",
            description = "Number of decimal places.",
            optional = true,
            defaultValue = "2",
            order = 100)
    FeaturerParamInt decimalPlaces = new FeaturerParamInt("decimalPlaces");

    @FeaturerParam(
            name = "Rounding mode",
            description = "Rounding mode for decimal scaling",
            optional = true,
            defaultValue = "HALF_UP",
            order = 101
    )
    FeaturerParamRoundingMode roundingMode = new FeaturerParamRoundingMode("roundingMode");


    default BigDecimal scaleAndRound(BigDecimal value, Properties properties) {
        if (value == null) {
            return value;
        }

        var scale = decimalPlaces.extract(properties);
        var roundingModeParam = roundingMode.extract(properties);

        if (scale != null) {
            value = value.setScale(scale, roundingModeParam);
        }

        return value;
    }
}
