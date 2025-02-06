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
@Featurer(id = FeaturerTwins.ID_2428,
        name = "Math compare output twin field value and context twin field value",
        description = "")
@Slf4j
public class ConditionerMathCompareOutputTwinFieldValueAndContextTwinTwinFieldValue extends Conditioner {

    @FeaturerParam(name = "Greater twin class field", description = "", order = 1)
    public static final FeaturerParamUUID greaterTwinClassField = new FeaturerParamUUIDTwinsTwinClassFieldId("greaterTwinClassField");

    @FeaturerParam(name = "Comparison twin class field", description = "", order = 2)
    public static final FeaturerParamUUID comparisonTwinClassField = new FeaturerParamUUIDTwinsTwinClassFieldId("comparisonTwinClassField");

    @FeaturerParam(name = "Equals", description = "", order = 3)
    public static final FeaturerParamBoolean equals = new FeaturerParamBoolean("equals");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        FieldValue greaterValue = fieldLookupers.getFromItemOutputDbFields().lookupFieldValue(factoryItem, greaterTwinClassField.extract(properties));
        FieldValue comparisonValue = fieldLookupers.getFromContextTwinUncommitedFields().lookupFieldValue(factoryItem, comparisonTwinClassField.extract(properties));
        double greater, comparison;
        if (greaterValue instanceof FieldValueText greaterValueText) {
            Number greaterNumber = NumberUtils.createNumber(greaterValueText.getValue());
            greater = greaterNumber.doubleValue();
        } else
            throw new ServiceException(ErrorCodeTwins.FACTORY_MULTIPLIER_ERROR, "greaterTwinClassField[" + greaterTwinClassField + "] is not instance of text field and can not be converted to number");
        if (comparisonValue instanceof FieldValueText comparisonValueText) {
            Number comparisonNumber = NumberUtils.createNumber(comparisonValueText.getValue());
            comparison = comparisonNumber.doubleValue();
        } else
            throw new ServiceException(ErrorCodeTwins.FACTORY_MULTIPLIER_ERROR, "comparisonTwinClassField[" + comparisonTwinClassField + "] is not instance of text field and can not be converted to number");
        return equals.extract(properties) ? greater >= comparison : greater > comparison;
    }

}
