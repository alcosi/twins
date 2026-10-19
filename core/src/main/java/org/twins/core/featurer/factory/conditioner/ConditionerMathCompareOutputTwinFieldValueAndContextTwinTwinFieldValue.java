package org.twins.core.featurer.factory.conditioner;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2428,
        name = "Math compare output twin field value and context twin field value",
        description = "Compares an output twin DB field (greater) against a context twin UNCOMMITED (in-memory) field (comparison). equals=true => greater >= comparison.")
public class ConditionerMathCompareOutputTwinFieldValueAndContextTwinTwinFieldValue extends ConditionerMathCompareOutputTwinFieldValueAndContextFieldBase {

    @Override
    protected FieldValue resolveComparisonValue(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return fieldLookupers.getFromContextTwinUncommitedFields().lookupFieldValue(factoryItem, comparisonTwinClassField.extract(properties));
    }
}
