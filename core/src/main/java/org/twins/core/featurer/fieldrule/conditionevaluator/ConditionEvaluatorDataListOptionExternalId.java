package org.twins.core.featurer.fieldrule.conditionevaluator;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptorDataListOptionExternalId;

import java.util.Properties;

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
}
