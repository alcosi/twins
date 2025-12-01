package org.twins.core.dao.specifications.twinclass;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.projection.ProjectionEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.Collection;
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

    public static Specification<TwinClassFieldEntity> checkSrcProjectionFieldIdIn(final Collection<UUID> ids, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(ids)) {
                return cb.conjunction();
            }

            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<ProjectionEntity> projection = subquery.from(ProjectionEntity.class);

            subquery.select(projection.get(ProjectionEntity.Fields.srcTwinClassFieldId))
                    .where(projection.get(ProjectionEntity.Fields.srcTwinClassFieldId).in(ids));

            return not ?
                    cb.not(root.get(TwinClassFieldEntity.Fields.id).in(subquery)) :
                    root.get(TwinClassFieldEntity.Fields.id).in(subquery);
        };
    }


    public static Specification<TwinClassFieldEntity> checkDstProjectionFieldIdIn(final Collection<UUID> fieldIds, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(fieldIds)) {
                return cb.conjunction();
            }

            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<ProjectionEntity> projection = subquery.from(ProjectionEntity.class);
            subquery.select(projection.get(ProjectionEntity.Fields.dstTwinClassFieldId))
                    .where(projection.get(ProjectionEntity.Fields.dstTwinClassFieldId).in(fieldIds));

            return not ?
                    cb.not(root.get(TwinClassFieldEntity.Fields.id).in(subquery)) :
                    root.get(TwinClassFieldEntity.Fields.id).in(subquery);
        };
    }

    public static Specification<TwinClassFieldEntity> checkDstProjectionClassIdIn(final Collection<UUID> classIds, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(classIds)) {
                return cb.conjunction();
            }

            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<ProjectionEntity> projection = subquery.from(ProjectionEntity.class);
            subquery.select(projection.get(ProjectionEntity.Fields.dstTwinClassFieldId))
                    .where(projection.get(ProjectionEntity.Fields.dstTwinClassId).in(classIds));

            return not ?
                    cb.not(root.get(TwinClassFieldEntity.Fields.id).in(subquery)) :
                    root.get(TwinClassFieldEntity.Fields.id).in(subquery);
        };
    }

    public static Specification<TwinClassFieldEntity> checkProjectionTypeIdIn(final Collection<UUID> typeIds, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(typeIds)) {
                return cb.conjunction();
            }

            // check as src
            Subquery<UUID> srcSubquery = query.subquery(UUID.class);
            Root<ProjectionEntity> srcProjection = srcSubquery.from(ProjectionEntity.class);
            srcSubquery.select(srcProjection.get(ProjectionEntity.Fields.srcTwinClassFieldId))
                    .where(srcProjection.get(ProjectionEntity.Fields.projectionTypeId).in(typeIds));

            // check as dst
            Subquery<UUID> dstSubquery = query.subquery(UUID.class);
            Root<ProjectionEntity> dstProjection = dstSubquery.from(ProjectionEntity.class);
            dstSubquery.select(dstProjection.get(ProjectionEntity.Fields.dstTwinClassFieldId))
                    .where(dstProjection.get(ProjectionEntity.Fields.projectionTypeId).in(typeIds));

            Predicate condition = cb.or(
                    root.get(TwinClassFieldEntity.Fields.id).in(srcSubquery),
                    root.get(TwinClassFieldEntity.Fields.id).in(dstSubquery)
            );

            return not ? cb.not(condition) : condition;
        };
    }
}
