package org.twins.core.dao.specifications.factory;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.Collection;
import java.util.UUID;

@Slf4j
public class FactoryPipelineStepSpecification extends CommonSpecification<TwinFactoryPipelineStepEntity> {

    public static Specification<TwinFactoryPipelineStepEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> createPredicateWithJoins(root, cb, domainId, (property, criteriaBuilder, filedValue) -> criteriaBuilder.or(criteriaBuilder.isNull(property), criteriaBuilder.equal(property, filedValue)), JoinType.INNER, TwinFactoryPipelineStepEntity.Fields.twinFactoryPipeline, TwinFactoryPipelineEntity.Fields.twinFactory, TwinFactoryEntity.Fields.domainId);
    }

    public static Specification<TwinFactoryPipelineStepEntity> checkFactoryIdIn(Collection<UUID> search, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            query.distinct(true);

            Join<TwinFactoryPipelineStepEntity, TwinFactoryPipelineEntity> join = root.join(TwinFactoryPipelineStepEntity.Fields.twinFactoryPipeline, JoinType.INNER);

            Predicate predicate = join.get(TwinFactoryPipelineEntity.Fields.twinFactoryId).in(search);
            if (not) predicate = cb.not(predicate);
            return predicate;
        };
    }
}
