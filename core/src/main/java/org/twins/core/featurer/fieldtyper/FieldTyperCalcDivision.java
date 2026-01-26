package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1337, name = "Division", description = "First / Second")
public class FieldTyperCalcDivision extends FieldTyperCalcBinaryBase {

    @FeaturerParam(name = "divisionByZeroResul", description = "Result if division by zero", defaultValue = "<n/a>")
    public static final FeaturerParamString divisionByZeroResul = new FeaturerParamString("divisionByZeroResul");

    @Override
    protected String calculate(Double v1, Double v2, Properties properties) throws ServiceException {
        double d1 = v1 != null ? v1 : 0.0;
        double d2 = v2 != null ? v2 : 0.0;
        if (d2 == 0) {
            return divisionByZeroResul.extract(properties);
        }
        return String.valueOf(d1 / d2);
    }
}
