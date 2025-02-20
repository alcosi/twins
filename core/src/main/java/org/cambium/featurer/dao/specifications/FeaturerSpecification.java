package org.cambium.featurer.dao.specifications;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

@Slf4j
@Component
public class FeaturerSpecification {

    public static Specification<FeaturerEntity> checkIntegerIn(final String field, final Set<Integer> ids, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(ids)) return cb.conjunction();
            return not ? cb.not(root.get(field).in(ids)) : root.get(field).in(ids);
        };
    }

}

