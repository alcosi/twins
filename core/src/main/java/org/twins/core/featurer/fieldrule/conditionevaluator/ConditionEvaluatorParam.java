package org.twins.core.featurer.fieldrule.conditionevaluator;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptorParam;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4502,
        name = "Condition Evaluator Basic",
        description = "Evaluates a basic condition")
public class ConditionEvaluatorParam extends ConditionEvaluator<ConditionDescriptorParam> {
    @FeaturerParam(name = "EvaluatedParamKey", description = "", order = 3)
    public static final FeaturerParamString evaluatedParamKey = new FeaturerParamString("evaluatedParamKey");

    @Override
    protected ConditionDescriptorParam getConditionDescriptor(TwinClassFieldConditionEntity twinClassFieldConditionEntity, Properties properties) throws ServiceException {
        ConditionDescriptorParam descriptor = new ConditionDescriptorParam();
        descriptor.evaluatedParamKey(evaluatedParamKey.extract(properties))
                .valueToCompareWith(valueToCompareWith.extract(properties))
                .conditionOperator(conditionOperator.extract(properties));
        return descriptor;
    }

    @Override
    protected boolean evaluate(TwinClassFieldConditionEntity twinClassFieldConditionEntity, Properties properties, FieldValue currentValue) throws ServiceException {
        String paramKey = evaluatedParamKey.extract(properties);
        String actualValue = twinClassFieldConditionEntity.getConditionEvaluatorParams() != null
                ? twinClassFieldConditionEntity.getConditionEvaluatorParams().get(paramKey)
                : null;
        var operator = conditionOperator.extract(properties);
        String expected = valueToCompareWith.extract(properties);
        return evaluateOperator(actualValue, operator, expected);
    }
}
