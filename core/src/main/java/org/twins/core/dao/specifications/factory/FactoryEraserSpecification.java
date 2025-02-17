package org.twins.core.dao.specifications.factory;

import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.UUID;

@Slf4j
public class FactoryEraserSpecification extends CommonSpecification<TwinFactoryEraserEntity> {

    public static Specification<TwinFactoryEraserEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> createPredicateWithJoins(root, cb, domainId, (property, criteriaBuilder, filedValue) -> criteriaBuilder.or(criteriaBuilder.isNull(property), criteriaBuilder.equal(property, filedValue)), JoinType.INNER, TwinFactoryEraserEntity.Fields.twinFactory, TwinFactoryEntity.Fields.domainId);
    }
}
