package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.enums.twinclass.LogicOperator;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinClassFieldConditionSearch {

    public Set<UUID> idList;
    public Set<UUID> idExcludeList;
    public Set<UUID> twinClassFieldRuleIdList;
    public Set<UUID> twinClassFieldRuleIdExcludeList;
    public Set<UUID> baseTwinClassFieldIdList;
    public Set<UUID> baseTwinClassFieldIdExcludeList;
    public Set<UUID> parentTwinClassFieldConditionIdList;
    public Set<UUID> parentTwinClassFieldConditionIdExcludeList;
    public Set<LogicOperator> logicOperatorIdList;
    public Set<LogicOperator> logicOperatorIdExcludeList;
    public Set<Integer> conditionEvaluatorFeaturerIdList;
    public Set<Integer> conditionEvaluatorFeaturerIdExcludeList;
}
