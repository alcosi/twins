package org.twins.core.dao.specifications.comment;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.collectionUuidsToSqlArray;
import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class CommentSpecification extends CommonSpecification<TwinCommentEntity> {

    public static Specification<TwinCommentEntity> checkFieldLikeIn(final String field, final Collection<String> search, final boolean not, final boolean or) {
        return (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search)) {
                for (String name : search) {
                    Predicate predicate = cb.like(cb.lower(root.get(field)), "%" + name.toLowerCase() + "%");
                    if (not) predicate = cb.not(predicate);
                    predicates.add(predicate);
                }
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<TwinCommentEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            if (domainId == null)
                return cb.disjunction();
            Join<TwinCommentEntity, TwinEntity> joinTwin = root.join(TwinCommentEntity.Fields.twin, JoinType.INNER);
            Join<TwinEntity, TwinClassEntity> joinTwinClass = joinTwin.join(TwinEntity.Fields.twinClass, JoinType.INNER);
            return cb.equal(joinTwinClass.get(TwinClassEntity.Fields.domainId), domainId);
        };
    }

    public static Specification<TwinCommentEntity> checkPermissions(UUID domainId, UUID businessAccountId, UUID userId, Set<UUID> userGroups) throws ServiceException {
        return (root, query, cb) -> {
            Join<TwinCommentEntity, TwinEntity> joinTwin = root.join(TwinCommentEntity.Fields.twin, JoinType.INNER);

            Expression<UUID> spaceId = joinTwin.get(TwinEntity.Fields.permissionSchemaSpaceId);
            Expression<UUID> permissionIdTwin = joinTwin.get(TwinEntity.Fields.viewPermissionId);
            Expression<UUID> permissionIdTwinClass = joinTwin.join(TwinEntity.Fields.twinClass).get(TwinClassEntity.Fields.viewPermissionId);
            Expression<UUID> twinClassId = joinTwin.join(TwinEntity.Fields.twinClass).get(TwinClassEntity.Fields.id);

            Predicate isAssigneePredicate = cb.equal(joinTwin.get(TwinEntity.Fields.assignerUserId), cb.literal(userId));
            Predicate isCreatorPredicate = cb.equal(joinTwin.get(TwinEntity.Fields.createdByUserId), cb.literal(userId));

            return cb.isTrue(cb.function("permission_check", Boolean.class,
                    cb.literal(domainId),
                    cb.literal(businessAccountId),
                    spaceId,
                    permissionIdTwin,
                    permissionIdTwinClass,
                    cb.literal(userId),
                    cb.literal(collectionUuidsToSqlArray(userGroups)),
                    twinClassId,
                    cb.selectCase().when(isAssigneePredicate, cb.literal(true)).otherwise(cb.literal(false)),
                    cb.selectCase().when(isCreatorPredicate, cb.literal(true)).otherwise(cb.literal(false))
            ));
        };
    }
    public static Specification<TwinCommentEntity> localDateTimeBetween(final String field, final DataTimeRangeDTOv1 dateRange) {
        return (root, query, cb) -> {
            if (dateRange == null || (dateRange.getFrom() == null && dateRange.getTo() == null)) return cb.conjunction();

            Predicate predicate = cb.conjunction();
            if (dateRange.getFrom() != null)
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get(field), dateRange.getFrom()));
            if (dateRange.getTo() != null)
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get(field), dateRange.getTo()));
            return predicate;
        };
    }
}
