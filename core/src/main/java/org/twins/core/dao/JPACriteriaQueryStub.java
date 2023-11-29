package org.twins.core.dao;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class JPACriteriaQueryStub {
    private Path<?> select;
    private Predicate[] where;
}
