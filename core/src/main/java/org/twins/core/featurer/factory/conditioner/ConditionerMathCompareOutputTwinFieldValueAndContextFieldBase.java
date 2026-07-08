package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;

@Slf4j
public abstract class ConditionerMathCompareOutputTwinFieldValueAndContextFieldBase extends Conditioner {

    @FeaturerParam(name = "Greater twin class field", description = "Output twin DB field (the limit)", order = 1)
    public static final FeaturerParamUUID greaterTwinClassField = new FeaturerParamUUIDTwinsTwinClassFieldId("greaterTwinClassField");

    @FeaturerParam(name = "Comparison twin class field", description = "Context field compared against the greater one. Source depends on the concrete conditioner", order = 2)
    public static final FeaturerParamUUID comparisonTwinClassField = new FeaturerParamUUIDTwinsTwinClassFieldId("comparisonTwinClassField");

    @FeaturerParam(name = "Equals", description = "If true: greater >= comparison; if false: greater > comparison", order = 3)
    public static final FeaturerParamBoolean equals = new FeaturerParamBoolean("equals");

    protected abstract FieldValue resolveComparisonValue(Properties properties, FactoryItem factoryItem) throws ServiceException;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        FieldValue greaterValue = fieldLookupers.getFromItemOutputDbFields().lookupFieldValue(factoryItem, greaterTwinClassField.extract(properties));
        FieldValue comparisonValue = resolveComparisonValue(properties, factoryItem);
        double greater, comparison;
        if (greaterValue instanceof FieldValueText greaterValueText) {
            greater = NumberUtils.createNumber(greaterValueText.getValue()).doubleValue();
        } else {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "greaterTwinClassField[" + greaterTwinClassField + "] is not instance of text field and can not be converted to number");
        }
        if (comparisonValue instanceof FieldValueText comparisonValueText) {
            comparison = NumberUtils.createNumber(comparisonValueText.getValue()).doubleValue();
        } else {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "comparisonTwinClassField[" + comparisonTwinClassField + "] is not instance of text field and can not be converted to number");
        }
        return equals.extract(properties) ? greater >= comparison : greater > comparison;
    }
}
