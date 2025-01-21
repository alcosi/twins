package org.twins.core.dao.specifications.comment;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;

import java.util.Set;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.collectionUuidsToSqlArray;

public class CommentSpecification extends CommonSpecification<TwinCommentEntity> {


    public static Specification<TwinCommentEntity> checkClass(final ApiUser apiUser) throws ServiceException {
        UUID finalUserId;
        UUID finalBusinessAccountId;
        UUID finalDomainId = apiUser.getDomainId();
        if (apiUser.isBusinessAccountSpecified()) {
            finalBusinessAccountId = apiUser.getBusinessAccountId();
        } else {
            finalBusinessAccountId = null;
        }
        if (apiUser.isUserSpecified()) {
            finalUserId = apiUser.getUserId();
        } else {
            finalUserId = null;
        }
        return (root, query, cb) -> {
            Join<TwinCommentEntity, TwinEntity> joinTwin = root.join(TwinCommentEntity.Fields.twin, JoinType.INNER);
            Join<TwinClassEntity, TwinEntity> twinClass = joinTwin.join(TwinEntity.Fields.twinClass);
            Predicate domain = cb.equal(twinClass.get(TwinClassEntity.Fields.domainId), finalDomainId);

            Predicate predicate;
            if (finalUserId != null) {
                predicate = cb.and(cb.or(
                                cb.equal(joinTwin.get(TwinEntity.Fields.ownerUserId), finalUserId),
                                cb.isNull(joinTwin.get(TwinEntity.Fields.ownerUserId)
                                )
                        )
                );
            } else {
                predicate = cb.and(cb.isNull(joinTwin.get(TwinEntity.Fields.ownerUserId)));
            }

            if (finalBusinessAccountId != null) {
                predicate = cb.and(predicate, cb.or(
                        cb.equal(joinTwin.get(TwinEntity.Fields.ownerBusinessAccountId), finalBusinessAccountId),
                        cb.isNull(joinTwin.get(TwinEntity.Fields.ownerBusinessAccountId)))
                );
            } else {
                cb.and(predicate, cb.isNull(joinTwin.get(TwinEntity.Fields.ownerBusinessAccountId)));
            }
            return cb.and(domain, predicate);
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

}
