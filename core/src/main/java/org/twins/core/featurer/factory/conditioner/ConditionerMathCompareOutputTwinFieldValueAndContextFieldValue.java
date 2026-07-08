package org.twins.core.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2447,
        name = "Math compare output twin field value and context field value",
        description = "Compares an output twin DB field (greater) against a factory context field / context twin db field (comparison). equals=true => greater >= comparison.")
public class ConditionerMathCompareOutputTwinFieldValueAndContextFieldValue extends ConditionerMathCompareOutputTwinFieldValueAndContextFieldBase {

    @Override
    protected FieldValue resolveComparisonValue(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return fieldLookupers.getFromContextFieldsAndContextTwinDbFields().lookupFieldValue(factoryItem, comparisonTwinClassField.extract(properties));
    }
}
