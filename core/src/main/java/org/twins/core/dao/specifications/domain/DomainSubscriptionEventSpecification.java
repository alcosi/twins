package org.twins.core.dao.specifications.domain;

import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.domain.DomainSubscriptionEventEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.Set;

public class DomainSubscriptionEventSpecification extends CommonSpecification<DomainSubscriptionEventEntity> {

    public static Specification<DomainSubscriptionEventEntity> checkIntegerIn(final Set<Integer> ids, boolean not, final String field) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(ids)) return cb.conjunction();
            return not ? cb.not(root.get(field).in(ids)) : root.get(field).in(ids);
        };
    }
}
