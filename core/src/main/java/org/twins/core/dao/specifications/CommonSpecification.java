package org.twins.core.dao.specifications;

import jakarta.persistence.criteria.Predicate;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;

import java.util.*;
import java.util.stream.Collectors;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class CommonSpecification<T> {

    public static <T> Specification<T> checkUuidIn(final String uuidField, final Collection<UUID> uuids, boolean not, boolean ifNotIsTrueIncludeNullValues) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return not ?
                    (ifNotIsTrueIncludeNullValues ?
                            cb.or(root.get(uuidField).in(uuids).not(), root.get(uuidField).isNull())
                            : root.get(uuidField).in(uuids).not())
                    : root.get(uuidField).in(uuids);
        };
    }

    //    Use checkFieldLikeIn
    @Deprecated
    public static <T> Specification<T> checkFieldLikeContainsIn(final String field, final Collection<String> search, final boolean not, final boolean or) {
        return checkFieldLikeIn(field, search.stream().map(it -> "%" + it + "%").collect(Collectors.toSet()), not, or);
    }

    public static <T> Specification<T> checkFieldLikeIn(final String field, final Collection<String> search, final boolean not, final boolean or) {
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
}
