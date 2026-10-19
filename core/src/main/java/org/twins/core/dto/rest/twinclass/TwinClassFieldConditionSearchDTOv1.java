package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.twinclass.LogicOperator;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldConditionSearchV1")
public class TwinClassFieldConditionSearchDTOv1 {

    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twin class field rule id list")
    public Set<UUID> twinClassFieldRuleIdList;

    @Schema(description = "twin class field rule id exclude list")
    public Set<UUID> twinClassFieldRuleIdExcludeList;

    @Schema(description = "base twin class field id list")
    public Set<UUID> baseTwinClassFieldIdList;

    @Schema(description = "base twin class field id exclude list")
    public Set<UUID> baseTwinClassFieldIdExcludeList;

    @Schema(description = "parent twin class field condition id list")
    public Set<UUID> parentTwinClassFieldConditionIdList;

    @Schema(description = "parent twin class field condition id exclude list")
    public Set<UUID> parentTwinClassFieldConditionIdExcludeList;

    @Schema(description = "logic operator id list")
    public Set<LogicOperator> logicOperatorIdList;

    @Schema(description = "logic operator id exclude list")
    public Set<LogicOperator> logicOperatorIdExcludeList;

    @Schema(description = "condition evaluator featurer id list")
    public Set<Integer> conditionEvaluatorFeaturerIdList;

    @Schema(description = "condition evaluator featurer id exclude list")
    public Set<Integer> conditionEvaluatorFeaturerIdExcludeList;
}
