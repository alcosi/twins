package org.twins.core.domain;

import lombok.Data;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.domain.search.TwinFieldSearch;

import java.util.List;

@Data
public class TwinFieldClause {
    /**
     * Disjunction (OR) of field conditions
     */
    private List<TwinFieldSearch> conditions;

    public TwinFieldClause addCondition(TwinFieldSearch condition) {
        conditions = CollectionUtils.safeAdd(conditions, condition);
        return this;
    }
}
