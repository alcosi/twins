package org.twins.core.dto.rest.twinclass;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

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

    @Schema(description = "group number – conditions inside the same group are AND-ed; different groups are OR-ed")
    public Integer groupNo;

    @Schema(description = "condition descriptor", example = "")
    public TwinClassFieldConditionDescriptorDTO conditionDescriptor;
}
