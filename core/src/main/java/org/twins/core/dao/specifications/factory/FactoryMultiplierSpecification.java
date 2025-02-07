package org.twins.core.dao.specifications.factory;

import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.Ternary;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.Set;
import java.util.UUID;

@Slf4j
public class FactoryMultiplierSpecification extends CommonSpecification<TwinFactoryMultiplierEntity> {

    public static Specification<TwinFactoryMultiplierEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> createPredicateWithJoins(root, cb, domainId, (property, criteriaBuilder, filedValue) -> criteriaBuilder.or(criteriaBuilder.isNull(property), criteriaBuilder.equal(property, filedValue)), JoinType.INNER, TwinFactoryMultiplierEntity.Fields.twinFactory, TwinFactoryEntity.Fields.domainId);
    }

    public static Specification<TwinFactoryMultiplierEntity> checkIntegerIn(final Set<Integer> ids, boolean not, final String field) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(ids)) return cb.conjunction();
            return not ? cb.not(root.get(field).in(ids)) : root.get(field).in(ids);
        };
    }

    public static Specification<TwinFactoryMultiplierEntity> checkTernary(final String field, Ternary ternary) {
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
