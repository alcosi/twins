package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.math.BigDecimal;
import java.util.Properties;

@Component
@Featurer(
        id = FeaturerTwins.ID_1353,
        name = "Subtraction (materialization)",
        description = "Save first minus second field on serializeValue, and return saved total from database"
)
public class FieldTyperCalcSubtractionMater extends FieldTyperCalcBinaryMater {

    @Override
    protected BigDecimal calculate(BigDecimal v1, BigDecimal v2, Properties properties) throws ServiceException {
        var d1 = v1 != null ? v1 : BigDecimal.ZERO;
        var d2 = v2 != null ? v2 : BigDecimal.ZERO;

        return scaleAndRound(d1.subtract(d2), properties);
    }
}
