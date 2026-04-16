package org.twins.core.dao.trigger;

import jakarta.persistence.criteria.Predicate;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.Set;

public class TwinTriggerTaskSpecification extends CommonSpecification<TwinTriggerTaskEntity> {

    public static Specification<TwinTriggerTaskEntity> checkStatusLikeIn(Set<TwinTriggerTaskStatus> statuses, boolean exclude) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(statuses)) {
                return cb.conjunction();
            }

            return exclude
                    ? cb.not(root.get(TwinTriggerTaskEntity.Fields.statusId).in(statuses))
                    : root.get(TwinTriggerTaskEntity.Fields.statusId).in(statuses);
        };
    }
}
