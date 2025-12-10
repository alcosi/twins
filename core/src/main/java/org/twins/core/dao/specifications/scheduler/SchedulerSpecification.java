package org.twins.core.dao.specifications.scheduler;

import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.Set;

@Component
public class SchedulerSpecification extends CommonSpecification<SchedulerEntity> {

    public static Specification<SchedulerEntity> checkIntegerIn(final Set<Integer> ids, boolean not, final String field) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(ids)) return cb.conjunction();
            return not ? cb.not(root.get(field).in(ids)) : root.get(field).in(ids);
        };
    }
}
