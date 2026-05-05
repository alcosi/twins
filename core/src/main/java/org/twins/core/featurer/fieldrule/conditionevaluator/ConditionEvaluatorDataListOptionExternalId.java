package org.twins.core.featurer.fieldrule.conditionevaluator;

import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptorDataListOptionExternalId;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;

import java.util.Properties;
import java.util.stream.Collectors;

@Component
@Featurer(id = FeaturerTwins.ID_4503,
        name = "Condition evaluator by option external id",
        description = "Evaluates a basic condition against a selector field option external id")
public class ConditionEvaluatorDataListOptionExternalId extends ConditionEvaluator<ConditionDescriptorDataListOptionExternalId> {
    @Override
    protected ConditionDescriptorDataListOptionExternalId getConditionDescriptor(TwinClassFieldConditionEntity twinClassFieldConditionEntity, Properties properties) throws ServiceException {
        ConditionDescriptorDataListOptionExternalId descriptor = new ConditionDescriptorDataListOptionExternalId();
        descriptor.conditionOperator(conditionOperator.extract(properties))
                .valueToCompareWith(valueToCompareWith.extract(properties));
        return descriptor;
    }

    @Override
    protected boolean evaluate(TwinClassFieldConditionEntity twinClassFieldConditionEntity, Properties properties, FieldValue currentValue) throws ServiceException {
        String actualValue = normalizeDataListOptionValue(currentValue);
        var operator = conditionOperator.extract(properties);
        var expected = valueToCompareWith.extract(properties);
        return evaluateOperator(actualValue, operator, expected);
    }

    private static String normalizeDataListOptionValue(FieldValue currentValue) throws ServiceException {
        if (currentValue == null || currentValue.isEmpty())
            return null;
        if (!(currentValue instanceof FieldValueSelect select))
            throw new ServiceException(ErrorCodeTwins.CONFIGURATION_IS_INVALID, "Incorrect condition evaluator for {}. " +
                    "ConditionEvaluatorDataListOptionExternalId can be used only with FieldValueSelect", currentValue.getTwinClassField().logNormal());
        String actualValue = null;
        if (CollectionUtils.isNotEmpty(select.getItems())) {
            actualValue = select.getItems().stream()
                    .map(DataListOptionEntity::getExternalId)
                    .filter(id -> id != null && !id.isBlank())
                    .collect(Collectors.joining(","));
        }
        return actualValue;
    }
}
