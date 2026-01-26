package org.twins.core.featurer.fieldrule.conditionevaluator;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptorValue;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4501,
        name = "Condition Evaluator Value",
        description = "Evaluates a basic condition against a field value ")
public class ConditionEvaluatorValue extends ConditionEvaluator<ConditionDescriptorValue> {
    @Override
    protected ConditionDescriptorValue getConditionDescriptor(TwinClassFieldConditionEntity twinClassFieldConditionEntity, Properties properties) throws ServiceException {
        ConditionDescriptorValue descriptor = new ConditionDescriptorValue();
        descriptor.conditionOperator(conditionOperator.extract(properties))
                .valueToCompareWith(valueToCompareWith.extract(properties));
        return descriptor;
    }
}
