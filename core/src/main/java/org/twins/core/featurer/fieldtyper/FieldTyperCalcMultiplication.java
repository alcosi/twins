package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1322, name = "Multiplication", description = "First * Second")
public class FieldTyperCalcMultiplication extends FieldTyperCalcBinaryBase<TwinFieldStorageSimple> {

    @FeaturerParam(name = "replaceZeroWithOne", description = "if some filed value is null or 0, then mulitply on 1")
    public static final FeaturerParamBoolean replaceZeroWithOne = new FeaturerParamBoolean("replaceZeroWithOne");

    @Override
    protected String calculate(Double v1, Double v2, Properties properties) throws ServiceException {
        Boolean extractedReplaceZeroWithOne = replaceZeroWithOne.extract(properties);
        double d1 = prepare(v1, extractedReplaceZeroWithOne);
        double d2 = prepare(v2, extractedReplaceZeroWithOne);
        return String.valueOf(d1 * d2);
    }

    private double prepare(Double v, boolean replace) {
        if (replace) {
            return (v == null || v == 0) ? 1.0 : v;
        }
        return v == null ? 0.0 : v;
    }
}
