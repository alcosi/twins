package org.twins.core.dao.specifications.domain;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.cambium.common.util.Ternary;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.domain.LongRange;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TierSpecification extends CommonSpecification<TierEntity> {

    public static Specification<TierEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> createPredicateWithJoins(root, cb, domainId, (property, criteriaBuilder, filedValue) -> criteriaBuilder.or(criteriaBuilder.isNull(property), criteriaBuilder.equal(property, filedValue)), JoinType.INNER, TierEntity.Fields.domainId);
    }

    public static Specification<TierEntity> checkTernary(final String field, Ternary ternary) {
        return (root, query, cb) -> {
            if (ternary == null)
                return cb.conjunction();
            return switch (ternary) {
                case ONLY -> cb.isTrue(root.get(field));
                case ONLY_NOT -> cb.isFalse(root.get(field));
                default -> cb.conjunction();
            };
        };
    }
}