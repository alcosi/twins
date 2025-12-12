package org.twins.core.dao.specifications.twinclass;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.projection.ProjectionEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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

    public static Specification<TwinClassFieldEntity> buildProjectionSpec(
            String associationPath,
            Collection<UUID> srcIdList,
            Collection<UUID> dstIdList,
            Collection<UUID> projectionTypeIdList
    ) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(srcIdList) && CollectionUtils.isEmpty(dstIdList) &&
                    CollectionUtils.isEmpty(projectionTypeIdList)) {
                return cb.conjunction();
            }

            Join<TwinClassFieldEntity, ProjectionEntity> projectionJoin = root.join(associationPath, JoinType.INNER);

            List<Predicate> predicates = new ArrayList<>();

            if (!CollectionUtils.isEmpty(srcIdList)) {
                predicates.add(projectionJoin.get(ProjectionEntity.Fields.srcTwinClassFieldId).in(srcIdList));
            }

            if (!CollectionUtils.isEmpty(dstIdList)) {
                predicates.add(projectionJoin.get(ProjectionEntity.Fields.dstTwinClassFieldId).in(dstIdList));
            }

            if (!CollectionUtils.isEmpty(projectionTypeIdList)) {
                List<Predicate> typePredicates = new ArrayList<>();
                for (UUID typeId : projectionTypeIdList) {
                    typePredicates.add(cb.equal(
                            projectionJoin.get(ProjectionEntity.Fields.projectionTypeId),
                            typeId
                    ));
                }
                predicates.add(cb.or(typePredicates.toArray(new Predicate[0])));
            }

            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
