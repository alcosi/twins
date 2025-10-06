package org.twins.core.featurer.conditionevaluator.conditiondescriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldConditionElementType;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class ConditionDescriptorBasic extends ConditionDescriptor{
    private TwinClassFieldConditionElementType conditionElement;
    private String valueToCompareWith;
    private String evaluatedParamKey;
    private String compareParams;
}
