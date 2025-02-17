package org.twins.core.dao.specifications.factory;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.Collection;
import java.util.UUID;

@Slf4j
public class FactoryMultiplierFilterSpecification extends CommonSpecification<TwinFactoryMultiplierFilterEntity> {

    public static Specification<TwinFactoryMultiplierFilterEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> createPredicateWithJoins(root, cb, domainId, (property, criteriaBuilder, filedValue) -> criteriaBuilder.or(criteriaBuilder.isNull(property), criteriaBuilder.equal(property, filedValue)), JoinType.INNER, TwinFactoryMultiplierFilterEntity.Fields.multiplier, TwinFactoryMultiplierEntity.Fields.twinFactory, TwinFactoryEntity.Fields.domainId);
    }

    public static Specification<TwinFactoryMultiplierFilterEntity> checkFactoryIdIn(Collection<UUID> search, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            query.distinct(true);

            Join<TwinFactoryMultiplierFilterEntity, TwinFactoryMultiplierEntity> join = root.join(TwinFactoryMultiplierFilterEntity.Fields.multiplier, JoinType.INNER);

            Predicate predicate = join.get(TwinFactoryMultiplierEntity.Fields.twinFactoryId).in(search);
            if (not) predicate = cb.not(predicate);
            return predicate;
        };
    }
}
