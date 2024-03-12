package org.cambium.common.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpecificationUtils {

    public static Predicate getPredicate(CriteriaBuilder cb, List<Predicate> predicates, boolean or) {
        if (predicates.isEmpty()) return cb.conjunction();
        else {
            Predicate[] stockArr = new Predicate[predicates.size()];
            stockArr = predicates.toArray(stockArr);
            return or ? cb.or(stockArr) : cb.and(stockArr);
        }
    }

    // transform Set<UUID> to string (PostgreSQL array format);
    public static String collectionUuidsToSqlArray(Collection<UUID> ids) {
        return null == ids || ids.isEmpty() ? "{}" :
                "{" + ids.stream().map(UUID::toString).collect(Collectors.joining(",")) + "}";
    }


}
