package org.twins.core.domain.search;

import lombok.Data;
import org.cambium.common.util.CollectionUtils;

import java.util.List;

@Data
public class TwinFieldFilter {
    /**
     * Conjunctive Normal Form:
     * AND over OR-clauses.
     */
    private List<TwinFieldClause> clauses;

    public TwinFieldFilter addClause(TwinFieldClause clause) {
        CollectionUtils.safeAdd(clauses, clause);
        return this;
    }

    public boolean isEmpty() {
        return clauses == null || clauses.isEmpty();
    }
}
