package org.twins.core.dao.specifications.factory;

import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.UUID;


@Slf4j
public class FactoryPipelineSpecification extends CommonSpecification<TwinFactoryPipelineEntity> {

    public static Specification<TwinFactoryPipelineEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> createPredicateWithJoins(root, cb, domainId, (property, criteriaBuilder, filedValue) -> criteriaBuilder.or(criteriaBuilder.isNull(property), criteriaBuilder.equal(property, filedValue)), JoinType.INNER, TwinFactoryPipelineEntity.Fields.twinFactory, TwinFactoryEntity.Fields.domainId);
    }
}
