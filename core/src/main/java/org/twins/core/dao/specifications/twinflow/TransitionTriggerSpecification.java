package org.twins.core.dao.specifications.twinflow;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;

import java.util.Set;

@Slf4j
public class TransitionTriggerSpecification extends CommonSpecification<TwinflowTransitionTriggerEntity> {

    public static Specification<TwinflowTransitionTriggerEntity> checkIntegerIn(final Set<Integer> ids, boolean not, final String field) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(ids)) return cb.conjunction();
            return not ? cb.not(root.get(field).in(ids)) : root.get(field).in(ids);
        };
    }
}
