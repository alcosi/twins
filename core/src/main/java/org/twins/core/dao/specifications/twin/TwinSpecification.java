package org.twins.core.dao.specifications.twin;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinMarkerEntity;
import org.twins.core.dao.twin.TwinTagEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;

import java.util.*;

import static org.cambium.common.util.SpecificationUtils.collectionUuidsToSqlArray;
import static org.cambium.common.util.SpecificationUtils.getPredicate;
import static org.twins.core.dao.twinclass.TwinClassEntity.OwnerType.*;

@Slf4j
public class TwinSpecification {

    public static Specification<TwinEntity> checkTagIds(final Collection<UUID> tagIds, final boolean exclude) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(tagIds)) return cb.conjunction();
            Subquery<TwinTagEntity> tagSubquery = query.subquery(TwinTagEntity.class);
            Root<TwinTagEntity> tagRoot = tagSubquery.from(TwinTagEntity.class);
            tagSubquery.select(tagRoot);
            Predicate tagIdIn;
            tagIdIn = tagRoot.get(TwinTagEntity.Fields.tagDataListOptionId).in(tagIds);
            tagSubquery.where(
                    cb.and(
                            tagIdIn,
                            cb.equal(tagRoot.get(TwinTagEntity.Fields.twinId), root.get(TwinEntity.Fields.id))
                    )
            );
            return exclude ? cb.not(cb.exists(tagSubquery)) : cb.exists(tagSubquery);
        };
    }

    public static Specification<TwinEntity> checkMarkerIds(final Collection<UUID> markerIds, final boolean exclude) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(markerIds)) return cb.conjunction();
            Subquery<TwinMarkerEntity> markerSubquery = query.subquery(TwinMarkerEntity.class);
            Root<TwinMarkerEntity> markerRoot = markerSubquery.from(TwinMarkerEntity.class);
            markerSubquery.select(markerRoot);
            Predicate markerIdIn;
            markerIdIn = markerRoot.get(TwinMarkerEntity.Fields.markerDataListOptionId).in(markerIds);
            markerSubquery.where(
                    cb.and(
                            markerIdIn,
                            cb.equal(markerRoot.get(TwinMarkerEntity.Fields.twinId), root.get(TwinEntity.Fields.id))
                    )
            );
            return exclude ? cb.not(cb.exists(markerSubquery)) : cb.exists(markerSubquery);
        };
    }


    public static Specification<TwinEntity> checkPermissions(UUID domainId, UUID businessAccountId, UUID userId, Set<UUID> userGroups) throws ServiceException {
        return (root, query, cb) -> {

            Expression<UUID> spaceId = root.get(TwinEntity.Fields.permissionSchemaSpaceId);
            Expression<UUID> permissionIdTwin = root.get(TwinEntity.Fields.viewPermissionId);
            Expression<UUID> permissionIdTwinClass = root.join(TwinEntity.Fields.twinClass).get(TwinClassEntity.Fields.viewPermissionId);
            Expression<UUID> twinClassId = root.join(TwinEntity.Fields.twinClass).get(TwinClassEntity.Fields.id);

            Predicate isAssigneePredicate = cb.equal(root.get(TwinEntity.Fields.assignerUserId), cb.literal(userId));
            Predicate isCreatorPredicate = cb.equal(root.get(TwinEntity.Fields.createdByUserId), cb.literal(userId));

            return cb.isTrue(cb.function("permissionCheck", Boolean.class,
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

    public static Specification<TwinEntity> checkHierarchyContainsAny(String field, final Set<UUID> hierarchyTreeContainsIdList) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(hierarchyTreeContainsIdList)) return cb.conjunction();
            List<Predicate> predicates = new ArrayList<>();
            for (UUID id : hierarchyTreeContainsIdList) {
                String ltreeId = "*." + id.toString().replace("-", "_") + ".*";
                Expression<String> hierarchyTreeExpression = root.get(field);
                predicates.add(cb.isTrue(cb.function("hierarchy_check_lquery", Boolean.class, hierarchyTreeExpression, cb.literal(ltreeId))));
            }
            return getPredicate(cb, predicates, true);
        };
    }

    public static Specification<TwinEntity> checkUuidIn(final String uuidField, final Collection<UUID> uuids, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return not ? root.get(uuidField).in(uuids).not() : root.get(uuidField).in(uuids);
        };
    }

    public static Specification<TwinEntity> checkFieldLikeIn(final String field, final Collection<String> search, final boolean or) {
        return (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search))
                for (String s : search) {
                    Predicate predicate = cb.like(cb.lower(root.get(field)), s.toLowerCase());
                    predicates.add(predicate);
                }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<TwinEntity> checkClass(final Collection<UUID> twinClassUuids, final ApiUser apiUser) throws ServiceException {
        UUID finalUserId;
        UUID finalBusinessAccountId;
        if (apiUser.isBusinessAccountSpecified())
            finalBusinessAccountId = apiUser.getBusinessAccountId();
        else {
            finalBusinessAccountId = null;
        }
        if (apiUser.isUserSpecified())
            finalUserId = apiUser.getUserId();
        else {
            finalUserId = null;
        }
        return (twin, query, cb) -> {
            if (!CollectionUtils.isEmpty(twinClassUuids)) {
                List<Predicate> predicates = new ArrayList<>();
                for (UUID twinClassId : twinClassUuids) {
                    Predicate checkClassId = cb.equal(twin.get(TwinEntity.Fields.twinClassId), twinClassId);
                    predicates.add(checkClassId);
                }
                Join<TwinClassEntity, TwinEntity> twinClass = twin.join(TwinEntity.Fields.twinClass);
                Predicate joinPredicateSystemLevel = cb.equal(twinClass.get(TwinClassEntity.Fields.ownerType), SYSTEM);
                Predicate joinPredicateUserLevel = cb.or(
                        cb.equal(twinClass.get(TwinClassEntity.Fields.ownerType), USER),
                        cb.equal(twinClass.get(TwinClassEntity.Fields.ownerType), DOMAIN_BUSINESS_ACCOUNT_USER)
                );
                Predicate joinPredicateBusinessLevel = cb.or(
                        cb.equal(twinClass.get(TwinClassEntity.Fields.ownerType), BUSINESS_ACCOUNT),
                        cb.equal(twinClass.get(TwinClassEntity.Fields.ownerType), DOMAIN_BUSINESS_ACCOUNT),
                        cb.equal(twinClass.get(TwinClassEntity.Fields.ownerType), DOMAIN_BUSINESS_ACCOUNT_USER)
                );

                Predicate rootPredicateUser = cb.equal(twin.get(TwinEntity.Fields.ownerUserId), finalUserId);
                Predicate rootPredicateBusiness = cb.equal(twin.get(TwinEntity.Fields.ownerBusinessAccountId), finalBusinessAccountId);

                return cb.and(
                        getPredicate(cb, predicates, true),
                        cb.or(
                                cb.and(joinPredicateUserLevel, rootPredicateUser),
                                cb.and(joinPredicateBusinessLevel, rootPredicateBusiness)
                                //todo system level:  add Subquery to detect valid user and business account twins
                        )
                );
            } else { // no class filter, so we have to add force filtering by owner
                Predicate predicate;
                if (apiUser.isUserSpecified()) {
                    predicate = cb.and(cb.or(
                                    cb.equal(twin.get(TwinEntity.Fields.ownerUserId), finalUserId),
                                    cb.isNull(twin.get(TwinEntity.Fields.ownerUserId)
                                    )
                            )
                    );
                } else predicate = cb.and(cb.isNull(twin.get(TwinEntity.Fields.ownerUserId)));

                if (apiUser.isBusinessAccountSpecified()) {
                    predicate = cb.and(predicate, cb.or(
                                    cb.equal(twin.get(TwinEntity.Fields.ownerBusinessAccountId), finalBusinessAccountId),
                                    cb.isNull(twin.get(TwinEntity.Fields.ownerBusinessAccountId)
                                    )
                            )
                    );
                } else cb.and(predicate, cb.isNull(twin.get(TwinEntity.Fields.ownerBusinessAccountId)));
                return predicate;
            }
        };
    }

    public static Specification<TwinEntity> checkTwinLinks(Map<UUID, Set<UUID>> twinLinksMap, Map<UUID, Set<UUID>> noTwinLinksMap) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            if (MapUtils.isNotEmpty(twinLinksMap)) {
                Join<TwinEntity, TwinLinkEntity> linkSrcTwinInner = root.join(TwinEntity.Fields.linksBySrcTwinId, JoinType.INNER);
                for (Map.Entry<UUID, Set<UUID>> entry : twinLinksMap.entrySet()) {
                    Predicate linkCondition = cb.equal(linkSrcTwinInner.get(TwinLinkEntity.Fields.linkId), entry.getKey());
                    Predicate dstTwinCondition = entry.getValue().isEmpty() ? cb.conjunction() : linkSrcTwinInner.get(TwinLinkEntity.Fields.dstTwinId).in(entry.getValue());
                    predicates.add(cb.and(linkCondition, dstTwinCondition));
                }
            }
            Predicate include = predicates.isEmpty() ? cb.conjunction() : cb.or(predicates.toArray(new Predicate[0]));

            predicates = new ArrayList<>();
            if (MapUtils.isNotEmpty(noTwinLinksMap)) {
                Join<TwinEntity, TwinLinkEntity> linkSrcTwinLeft = root.join(TwinEntity.Fields.linksBySrcTwinId, JoinType.LEFT);
                for (Map.Entry<UUID, Set<UUID>> entry : noTwinLinksMap.entrySet()) {
                    Predicate linkCondition, dstTwinCondition, srcTwinCondition, nullSrcTwin, interimPredicate, unitedPredicate;
                    if (entry.getValue().isEmpty()) {
                        linkCondition = cb.equal(linkSrcTwinLeft.get(TwinLinkEntity.Fields.linkId), entry.getKey()).not();
                        nullSrcTwin = cb.isNull(linkSrcTwinLeft.get(TwinLinkEntity.Fields.linkId));
                        srcTwinCondition = cb.isNull(linkSrcTwinLeft.get(TwinLinkEntity.Fields.srcTwinId));
                        interimPredicate = cb.or(linkCondition, nullSrcTwin);
                        unitedPredicate = cb.and(interimPredicate, srcTwinCondition);
                    } else {
                        linkCondition = cb.equal(linkSrcTwinLeft.get(TwinLinkEntity.Fields.linkId), entry.getKey());
                        dstTwinCondition = linkSrcTwinLeft.get(TwinLinkEntity.Fields.dstTwinId).in(entry.getValue()).not();
                        nullSrcTwin = cb.isNull(linkSrcTwinLeft.get(TwinLinkEntity.Fields.linkId));
                        unitedPredicate = cb.or(cb.and(linkCondition, dstTwinCondition), nullSrcTwin);
                    }
                    predicates.add(unitedPredicate);
                }
            }
            Predicate exclude = predicates.isEmpty() ? cb.conjunction() : cb.or(predicates.toArray(new Predicate[0]));

            return cb.and(include, exclude);
        };
    }
}
