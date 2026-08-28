package org.twins.core.featurer.fieldrule.conditionevaluator;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptorValue;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.HashMap;
import java.util.Properties;

/**
 * Always-true {@link ConditionEvaluator}. Useful as a no-op / always-pass condition. Overrides the public
 * entry to short-circuit before {@code extractProperties}, so it needs no params and ignores the value.
 */
@Component
@Featurer(id = FeaturerTwins.ID_4504,
        name = "Condition Evaluator True",
        description = "Always evaluates to true. No-op / always-pass condition; ignores params and the current value.")
public class ConditionEvaluatorTrue extends ConditionEvaluator<ConditionDescriptorValue> {

    @Override
    public boolean evaluate(HashMap<String, String> params, FieldValue currentValue) throws ServiceException {
        return true;
    }

    @Override
    protected ConditionDescriptorValue getConditionDescriptor(Properties properties) throws ServiceException {
        return new ConditionDescriptorValue();
    }

    @Override
    protected boolean evaluate(Properties properties, FieldValue currentValue) throws ServiceException {
        return true;
    }
}
