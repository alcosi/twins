package org.twins.core.dao.specifications.factory;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.Collection;

@Slf4j
public class FactoryConditionSpecification extends CommonSpecification<TwinFactoryConditionEntity> {

    public static Specification<TwinFactoryConditionEntity> checkFieldConditionerFeaturerIdIn(final Collection<Integer> ids, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(ids)) {
                return cb.conjunction();
            }

            String fieldName = TwinFactoryConditionEntity.Fields.conditionerFeaturerId;

            return not ?
                    cb.not(root.get(fieldName).in(ids)) :
                    root.get(fieldName).in(ids);
        };
    }
}
