package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.exception.ErrorCodeTwins;
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
    protected String calculate(Double v1, Double v2, Properties properties) throws ServiceException {
        boolean replace = replaceZeroWithOne.extract(properties);
        Integer scale = decimalPlaces.extract(properties);
        boolean doRound = round.extract(properties);

        BigDecimal d1 = prepare(v1, replace);
        BigDecimal d2 = prepare(v2, replace);

        BigDecimal result = d1.multiply(d2);

        if (scale != null) {
            if (doRound) {
                result = result.setScale(scale, RoundingMode.HALF_UP);
            } else {
                if (result.scale() > scale) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Result has more decimal places than allowed: " + result.toPlainString()
                    );
                }
            }
            result = result.stripTrailingZeros();
        }

        return result.toPlainString();
    }

    private BigDecimal prepare(Double v, boolean replace) {
        if (replace && (v == null || v == 0)) {
            return BigDecimal.ONE;
        }
        return v == null ? BigDecimal.ZERO : BigDecimal.valueOf(v);
    }
}
