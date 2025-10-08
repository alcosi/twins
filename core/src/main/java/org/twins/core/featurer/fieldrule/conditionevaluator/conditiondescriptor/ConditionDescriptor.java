package org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.twinclass.TwinClassFieldConditionOperator;

@Data
@Accessors(fluent = true)
public abstract class ConditionDescriptor {
    private String valueToCompareWith;
    private TwinClassFieldConditionOperator conditionOperator;
}
