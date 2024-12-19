package org.twins.core.dao.specifications.factory;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.Ternary;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.ArrayList;
import java.util.Collection;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

@Slf4j
public class FactoryPipelineSpecification extends CommonSpecification<TwinFactoryPipelineEntity> {

    public static Specification<TwinFactoryPipelineEntity> checkFieldLikeIn(final String field, final Collection<String> search, final boolean not, final boolean or) {
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

    public static Specification<TwinFactoryPipelineEntity> checkTernary(final String field, Ternary required) {
        return (root, query, cb) -> {
            if (required == null)
                return cb.conjunction();
            return switch (required) {
                case ONLY -> cb.isTrue(root.get(field));
                case ONLY_NOT -> cb.isFalse(root.get(field));
                default -> cb.conjunction();
            };
        };
    }

}
