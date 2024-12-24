package org.twins.core.dao.specifications.factory;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.Ternary;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

@Slf4j
public class FactoryPipelineStepSpecification extends CommonSpecification<TwinFactoryPipelineStepEntity> {

    public static Specification<TwinFactoryPipelineStepEntity> checkIntegerIn(final Collection<Integer> ids, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(ids)) return cb.conjunction();
            String field = TwinFactoryPipelineStepEntity.Fields.fillerFeaturerId;
            return not ? root.get(field).in(ids).not() : root.get(field).in(ids);
        };
    }

    public static Specification<TwinFactoryPipelineStepEntity> checkFieldLikeIn(final String field, final Collection<String> search, final boolean not, final boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            ArrayList<Predicate> predicates = new ArrayList<>();
            for (String name : search) {
                Predicate predicate = cb.like(cb.lower(root.get(field)), name.toLowerCase());
                if (not) predicate = cb.not(predicate);
                predicates.add(predicate);
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<TwinFactoryPipelineStepEntity> checkOptional(Ternary optional) {
        return (root, query, cb) -> {
            if (optional == null)
                return cb.conjunction();
            return switch (optional) {
                case ONLY -> cb.isTrue(root.get(TwinFactoryPipelineStepEntity.Fields.optional));
                case ONLY_NOT -> cb.isFalse(root.get(TwinFactoryPipelineStepEntity.Fields.optional));
                default -> cb.conjunction();
            };
        };
    }

}
