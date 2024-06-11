package org.twins.core.dao.specifications.featurer;

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

    public static Specification<FeaturerEntity> checkIntegerIn(final String field, final Set<Integer> listId) {
        return (root, query, cb) ->
                CollectionUtils.isEmpty(listId) ? cb.conjunction() : root.get(field).in(listId);
    }

    public static Specification<FeaturerEntity> checkStringIn(final String field, final Set<String> typeIdList) {
        return (root, query, cb) ->
                CollectionUtils.isEmpty(typeIdList) ? cb.conjunction() : root.get(field).in(typeIdList);
    }

    public static Specification<FeaturerEntity> checkFieldLikeIn(final String fieldName, final Collection<String> search, final boolean or) {
        return (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search))
                for (String s : search) {
                    Predicate predicate = cb.like(cb.lower(root.get(fieldName)), s.toLowerCase());
                    predicates.add(predicate);
                }
            return getPredicate(cb, predicates, or);
        };
    }
}

