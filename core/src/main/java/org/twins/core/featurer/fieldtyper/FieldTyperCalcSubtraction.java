package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1321, name = "Subtraction", description = "First - Second")
public class FieldTyperCalcSubtraction extends FieldTyperCalcBinaryBase {

    @Override
    protected String calculate(Double v1, Double v2, Properties properties) throws ServiceException {
        double d1 = v1 != null ? v1 : 0.0;
        double d2 = v2 != null ? v2 : 0.0;
        return String.valueOf(d1 - d2);
    }
}
