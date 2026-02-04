package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.math.BigDecimal;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1338, name = "Multiplication", description = "First * Second")
public class FieldTyperCalcMultiplication extends FieldTyperCalcBinaryBase {

    @FeaturerParam(name = "replaceZeroWithOne", description = "if some filed value is null or 0, then mulitply on 1")
    public static final FeaturerParamBoolean replaceZeroWithOne = new FeaturerParamBoolean("replaceZeroWithOne");

    @Override
    protected String calculate(BigDecimal v1, BigDecimal v2, Properties properties) throws ServiceException {
        var extractedReplaceZeroWithOne = replaceZeroWithOne.extract(properties);
        var d1 = prepare(v1, extractedReplaceZeroWithOne);
        var d2 = prepare(v2, extractedReplaceZeroWithOne);

        return d1.multiply(d2).toPlainString();
    }

    private BigDecimal prepare(BigDecimal v, boolean replace) {
        if (replace) {
            return (v == null || v.equals(BigDecimal.ZERO)) ? BigDecimal.ONE : v;
        }

        return v == null ? BigDecimal.ZERO : v;
    }
}
