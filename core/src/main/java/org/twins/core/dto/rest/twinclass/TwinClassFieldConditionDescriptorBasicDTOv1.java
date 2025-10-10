package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.twinclass.TwinClassFieldConditionOperator;

@Data
@Accessors(fluent = true)
@Schema(name = "TwinClassFieldConditionDescriptorBasicV1")
public abstract class TwinClassFieldConditionDescriptorBasicDTOv1 implements TwinClassFieldConditionDescriptorDTO {

    @Schema(description = "Condition operator type", example = "eq")
    public TwinClassFieldConditionOperator conditionOperator;
    @Schema(description = "Value to compare with", example = "size")
    public String valueToCompareWith;


}
