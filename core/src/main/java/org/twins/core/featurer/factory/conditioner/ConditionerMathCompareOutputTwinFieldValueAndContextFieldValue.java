package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2447,
        name = "Math compare output twin field value and context field value",
        description = "Compares an output twin DB field (greater) against a factory context field / context twin db field (comparison). equals=true => greater >= comparison.")
@Slf4j
public class ConditionerMathCompareOutputTwinFieldValueAndContextFieldValue extends Conditioner {

    @FeaturerParam(name = "Greater twin class field", description = "Output twin DB field (the limit)", order = 1)
    public static final FeaturerParamUUID greaterTwinClassField = new FeaturerParamUUIDTwinsTwinClassFieldId("greaterTwinClassField");

    @FeaturerParam(name = "Comparison twin class field", description = "Field read from factory context (transition context field or context twin db)", order = 2)
    public static final FeaturerParamUUID comparisonTwinClassField = new FeaturerParamUUIDTwinsTwinClassFieldId("comparisonTwinClassField");

    @FeaturerParam(name = "Equals", description = "If true: greater >= comparison; if false: greater > comparison", order = 3)
    public static final FeaturerParamBoolean equals = new FeaturerParamBoolean("equals");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        FieldValue greaterValue = fieldLookupers.getFromItemOutputDbFields().lookupFieldValue(factoryItem, greaterTwinClassField.extract(properties));
        FieldValue comparisonValue = fieldLookupers.getFromContextFieldsAndContextTwinDbFields().lookupFieldValue(factoryItem, comparisonTwinClassField.extract(properties));
        double greater, comparison;
        if (greaterValue instanceof FieldValueText greaterValueText) {
            Number greaterNumber = NumberUtils.createNumber(greaterValueText.getValue());
            greater = greaterNumber.doubleValue();
        } else
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "greaterTwinClassField[" + greaterTwinClassField + "] is not instance of text field and can not be converted to number");
        if (comparisonValue instanceof FieldValueText comparisonValueText) {
            Number comparisonNumber = NumberUtils.createNumber(comparisonValueText.getValue());
            comparison = comparisonNumber.doubleValue();
        } else
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "comparisonTwinClassField[" + comparisonTwinClassField + "] is not instance of text field and can not be converted to number");
        return equals.extract(properties) ? greater >= comparison : greater > comparison;
    }
}
