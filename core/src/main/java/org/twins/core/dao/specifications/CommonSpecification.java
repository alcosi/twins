package org.twins.core.dao.specifications;

import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

public class CommonSpecification<T> {

    public static <T> Specification<T> checkUuidIn(final String uuidField, final Collection<UUID> uuids, boolean not, boolean ifNotIsTrueIncludeNullValues) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return not ?
                    (ifNotIsTrueIncludeNullValues ?
                            cb.or(cb.not(root.get(uuidField).in(uuids)), root.get(uuidField).isNull())
                            : cb.not(root.get(uuidField).in(uuids)))
                    : root.get(uuidField).in(uuids);
        };
    }


}
