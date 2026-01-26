package org.twins.core.dao.specifications.projection;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.projection.ProjectionEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.Collection;

@Slf4j
public class ProjectionSpecification extends CommonSpecification<ProjectionEntity> {

    public static Specification<ProjectionEntity> checkFieldProjectorIdIn(final Collection<Integer> ids, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(ids)) {
                return cb.conjunction();
            }

            String fieldName = ProjectionEntity.Fields.fieldProjectorFeaturerId;

            return not ?
                    cb.not(root.get(fieldName).in(ids)) :
                    root.get(fieldName).in(ids);
        };
    }
}
