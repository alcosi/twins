package org.twins.core.domain.field.rule;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.enums.twinclass.LogicOperator;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Data
public class ConditionNode {
    final TwinClassFieldConditionEntity condition;
    final LogicOperator logic;
    final List<ConditionNode> children = new ArrayList<>();
}
