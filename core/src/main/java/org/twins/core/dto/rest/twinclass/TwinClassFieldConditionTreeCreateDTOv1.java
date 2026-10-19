package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.twinclass.LogicOperator;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldConditionTreeCreateV1")
public class TwinClassFieldConditionTreeCreateDTOv1 {
    @NotNull
    @Schema(description = "base (source) twin class field id")
    public UUID baseTwinClassFieldId;

    @NotNull
    @Schema(description = "Condition evaluator featurer ID", example = "1")
    public Integer conditionEvaluatorFeaturerId;

    @Schema(description = "order of the condition inside the rule")
    public Integer conditionOrder;

    @Schema(description = "Condition evaluator parameters", example = "{}")
    public HashMap<String, String> conditionEvaluatorParams;

    @Schema(description = "logic operator", example = "AND")
    public LogicOperator logicOperator;

    @Valid
    @Schema(description = "list of atomic conditions that make up the rule")
    public List<TwinClassFieldConditionTreeCreateDTOv1> childConditions;
}
