package org.twins.core.featurer.conditionevaluator;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamStringTwinConditionElementType;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.conditionevaluator.conditiondescriptor.ConditionDescriptorBasic;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4501,
        name = "Condition Evaluator Basic",
        description = "Evaluates a basic condition")
public class ConditionEvaluatorBasic extends ConditionEvaluator<ConditionDescriptorBasic>{
    @FeaturerParam(name = "ValueToCompareWith", description = "", order = 1)
    private FeaturerParamString valueToCompareWith = new FeaturerParamString("valueToCompareWith");; // cmp_value VARCHAR NULL,
    @FeaturerParam(name = "EvaluatedElement", description = "", order = 2)
    private FeaturerParamStringTwinConditionElementType evaluatedElement = new FeaturerParamStringTwinConditionElementType("evaluatedElement");;  //enum
    @FeaturerParam(name = "EvaluatedParamKey", description = "", order = 3, optional = true)
    private FeaturerParamString evaluatedParamKey = new FeaturerParamString("evaluatedParamKey");; //eval_param_key VARCHAR NULL,
    @FeaturerParam(name = "CompareParams", description = "", order =4, optional = true)
    private FeaturerParamString compareParams = new FeaturerParamString("compareParams");; //cmp_params hstore NULL,


    @Override
    protected ConditionDescriptorBasic getConditionDescriptor(TwinClassFieldConditionEntity twinClassFieldConditionEntity, Properties properties) throws ServiceException {
        return new ConditionDescriptorBasic()
                .valueToCompareWith(valueToCompareWith.extract(properties))
                .conditionElement(evaluatedElement.extract(properties))
                .evaluatedParamKey(evaluatedParamKey.extract(properties))
                .compareParams(compareParams.extract(properties));


    }
}
