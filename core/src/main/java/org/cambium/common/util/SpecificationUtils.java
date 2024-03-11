package org.cambium.common.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;

import java.util.List;

public class SpecificationUtils {

    public static Predicate getPredicate(CriteriaBuilder cb, List<Predicate> predicates, boolean or) {
        if (predicates.isEmpty()) return cb.conjunction();
        else {
            Predicate[] stockArr = new Predicate[predicates.size()];
            stockArr = predicates.toArray(stockArr);
            return or ? cb.or(stockArr) : cb.and(stockArr);
        }
    }

}
