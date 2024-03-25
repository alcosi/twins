package org.twins.core.dao.specifications.twin_class;

import jakarta.persistence.criteria.Expression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.*;

@Slf4j
public class TwinClassSpecification {

    public static Specification<TwinClassEntity> checkHierarchyForChildren(String field, final UUID id) {
        return (root, query, cb) -> {
            String ltreeId = "*." + id.toString().replace("-", "_") + ".*";
            Expression<String> hierarchyTreeExpression = root.get(field);
            return cb.isTrue(cb.function("hierarchyCheck", Boolean.class, hierarchyTreeExpression, cb.literal(ltreeId)));
        };
    }
}
