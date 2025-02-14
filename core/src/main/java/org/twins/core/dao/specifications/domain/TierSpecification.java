package org.twins.core.dao.specifications.domain;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.UUID;

public class TierSpecification extends CommonSpecification<TierEntity> {

    public static Specification<TierEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> createPredicateWithJoins(root, cb, domainId, (property, criteriaBuilder, filedValue) -> criteriaBuilder.or(criteriaBuilder.isNull(property), criteriaBuilder.equal(property, filedValue)), JoinType.INNER, TierEntity.Fields.domainId);
    }
}