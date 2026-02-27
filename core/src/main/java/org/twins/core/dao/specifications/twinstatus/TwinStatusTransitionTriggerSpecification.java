package org.twins.core.dao.specifications.twinstatus;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity;

import java.util.Set;

public class TwinStatusTransitionTriggerSpecification extends CommonSpecification<TwinStatusTransitionTriggerEntity> {

    public static Specification<TwinStatusTransitionTriggerEntity> checkTransitionTypeIn(Set<TwinStatusTransitionTriggerEntity.TransitionType> types, boolean exclude) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(types)) {
                return cb.conjunction();
            }
            return exclude
                    ? cb.not(root.get(TwinStatusTransitionTriggerEntity.Fields.type).in(types))
                    : root.get(TwinStatusTransitionTriggerEntity.Fields.type).in(types);
        };
    }
}
