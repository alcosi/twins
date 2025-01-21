package org.twins.core.dao.specifications.factory;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.Ternary;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.ArrayList;
import java.util.Collection;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

@Slf4j
public class FactoryPipelineStepSpecification extends CommonSpecification<TwinFactoryPipelineStepEntity> {

    public static Specification<TwinFactoryPipelineStepEntity> checkIntegerIn(final String field, final Collection<Integer> ids, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(ids)) return cb.conjunction();
            return not ? cb.not(root.get(field).in(ids)) : root.get(field).in(ids);
        };
    }

    public static Specification<TwinFactoryPipelineStepEntity> checkTernary(final String field, Ternary optional) {
        return (root, query, cb) -> {
            if (optional == null)
                return cb.conjunction();
            return switch (optional) {
                case ONLY -> cb.isTrue(root.get(field));
                case ONLY_NOT -> cb.isFalse(root.get(field));
                default -> cb.conjunction();
            };
        };
    }

}
