package org.twins.core.domain.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionTreeCreateDTOv1;
import org.twins.core.enums.twinclass.LogicOperator;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinClassFieldConditionTree {
    private UUID twinClassFieldRuleId;
    private UUID baseTwinClassFieldId;
    private Integer conditionOrder;
    private Integer conditionEvaluatorFeaturerId;
    private HashMap<String, String> conditionEvaluatorParams;
    private LogicOperator logicOperator;
    private List<TwinClassFieldConditionTree> childConditions;
}
