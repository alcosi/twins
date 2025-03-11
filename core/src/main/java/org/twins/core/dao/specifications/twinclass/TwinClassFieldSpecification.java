package org.twins.core.dao.specifications.twinclass;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.Collection;

@Slf4j
public class TwinClassFieldSpecification extends CommonSpecification<TwinClassFieldEntity> {

    public static Specification<TwinClassFieldEntity> checkFieldTyperIdIn(final Collection<Integer> ids, boolean not, boolean ifNotIsTrueIncludeNullValues) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(ids))
                return cb.conjunction();
            String id = TwinClassFieldEntity.Fields.fieldTyperFeaturerId;
            return not ?
                    (ifNotIsTrueIncludeNullValues ?
                            cb.or(cb.not(root.get(id).in(ids)), root.get(id).isNull())
                            : cb.not(root.get(id).in(ids)))
                    : root.get(id).in(ids);
        };
    }
}
