package org.twins.core.dto.rest.twinclass;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.twinclass.LogicOperator;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldConditionV1")
public class TwinClassFieldConditionDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "rule id this condition belongs to")
    public UUID ruleId;

    @Schema(description = "base (source) twin class field id")
    public UUID baseTwinClassFieldId;

    @Schema(description = "order of the condition inside the rule")
    public Integer conditionOrder;

    @Schema(description = "condition descriptor", example = "")
    public TwinClassFieldConditionDescriptorDTO conditionDescriptor;

    @Schema(description = "parent twin class field condition id")
    public UUID parentTwinClassFieldConditionId;

    @Schema(description = "logic operator id", example = "AND")
    public LogicOperator logicOperatorId;
}
