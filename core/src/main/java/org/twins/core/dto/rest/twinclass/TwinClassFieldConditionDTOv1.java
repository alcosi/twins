package org.twins.core.dto.rest.twinclass;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldConditionElementType;
import org.twins.core.dao.twinclass.TwinClassFieldConditionOperator;

import java.util.Map;
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

    @Schema(description = "comparison operator, e.g. eq, neq, lt, gt, contains")
    public TwinClassFieldConditionOperator conditionOperator;

    @Schema(description = "right-hand side value to compare with")
    public String cmpValue;

    @Schema(description = "optional comparison parameters")
    public Map<String, String> cmpParams;

    @Schema(description = "what part of the base field the rule will affect: value | param")
    public TwinClassFieldConditionElementType evaluatedElement;

    @Schema(description = "parameter key that should be evaluated if evaluatedElement is 'param'")
    public String evaluatedParamKey;
}
