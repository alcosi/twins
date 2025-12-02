package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.twinclass.LogicOperator;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldConditionCreateV1")
public class TwinClassFieldConditionCreateDTOv1 {

    @Schema(description = "base (source) twin class field id")
    public UUID baseTwinClassFieldId;

    @Schema(description = "order of the condition inside the rule")
    public Integer conditionOrder;

    @Schema(description = "Condition evaluator featurer ID", example = "1")
    public Integer conditionEvaluatorFeaturerId;

    @Schema(description = "Condition evaluator parameters", example = "{}")
    public HashMap<String, String> conditionEvaluatorParams;

    @Schema(description = "parent condition id")
    public UUID parentTwinClassFieldConditionId;

    @Schema(description = "logic operator", example = "AND")
    public LogicOperator logicOperator;

}
