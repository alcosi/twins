package org.twins.core.domain.twinclass;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinClassFieldRuleSave {
    private TwinClassFieldRuleEntity twinClassFieldRule;
    private Set<UUID> twinClassFieldIds;
    private List<TwinClassFieldConditionTree> twinClassFieldConditionTrees;
}
