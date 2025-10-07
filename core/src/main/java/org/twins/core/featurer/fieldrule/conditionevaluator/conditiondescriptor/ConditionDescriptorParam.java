package org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class ConditionDescriptorParam extends ConditionDescriptor{
    private String evaluatedParamKey;
}
