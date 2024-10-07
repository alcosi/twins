package org.twins.core.dao.specifications.twin;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twin.*;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.cambium.common.util.SpecificationUtils.collectionUuidsToSqlArray;
import static org.cambium.common.util.SpecificationUtils.getPredicate;
import static org.twins.core.dao.twinclass.TwinClassEntity.OwnerType.*;

@Slf4j
public class TwinSpecification {

    public static Specification<TwinEntity> checkHeadTwin(Specification<TwinEntity> headSpecification, TwinSearch headSearch) {
        return (root, query, cb) -> {
            if (null == headSpecification) return cb.conjunction();
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<TwinEntity> subRoot = subquery.from(TwinEntity.class);

            List<Predicate> classPredicates = new ArrayList<>();
            Predicate classPredicate = null;
            if (!CollectionUtils.isEmpty(headSearch.getTwinClassIdList())) {
                for (UUID twinClassId : headSearch.getTwinClassIdList())
                    classPredicates.add(cb.equal(subRoot.get(TwinEntity.Fields.twinClassId), twinClassId));
                classPredicate = getPredicate(cb, classPredicates, true);
            }
            subquery.select(subRoot.get(TwinEntity.Fields.id)).where(
                    headSpecification.toPredicate(subRoot, query, cb),
                    null != classPredicate ? classPredicate : cb.conjunction()
            );
            return cb.in(root.get(TwinEntity.Fields.headTwinId)).value(subquery);
        };
    }

    public static Specification<TwinEntity> checkChildrenTwins(Specification<TwinEntity> childrenSpecification, TwinSearch childrenSearch) {
        return (root, query, cb) -> {
            if (null == childrenSpecification) return cb.conjunction();
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<TwinEntity> subRoot = subquery.from(TwinEntity.class);
            List<Predicate> classPredicates = new ArrayList<>();
            Predicate classPredicate = null;
            if (!CollectionUtils.isEmpty(childrenSearch.getTwinClassIdList())) {
                for (UUID twinClassId : childrenSearch.getTwinClassIdList())
                    classPredicates.add(cb.equal(subRoot.get(TwinEntity.Fields.twinClassId), twinClassId));
                classPredicate = getPredicate(cb, classPredicates, true);
            }
            subquery.select(subRoot.get(TwinEntity.Fields.headTwinId)).where(
                    childrenSpecification.toPredicate(subRoot, query, cb),
                    null != classPredicate ? classPredicate : cb.conjunction()
            );
            return cb.in(root.get(TwinEntity.Fields.id)).value(subquery);
        };
    }

    public static Specification<TwinEntity> checkTagIds(final Collection<UUID> tagIds, final boolean exclude) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(tagIds)) return cb.conjunction();
            Join<TwinEntity, TwinTagEntity> tagJoin = root.join(TwinEntity.Fields.tags, JoinType.LEFT);
            Predicate tagIdIn = tagJoin.get(TwinTagEntity.Fields.tagDataListOptionId).in(tagIds);
            query.distinct(true);
            if (exclude) {
                Predicate noTags = cb.isNull(tagJoin.get(TwinTagEntity.Fields.twinId));
                return cb.or(noTags, cb.not(tagIdIn));
            } else {
                return cb.and(cb.isNotNull(tagJoin.get(TwinTagEntity.Fields.twinId)), tagIdIn);
            }
        };
    }

    public static Specification<TwinEntity> checkMarkerIds(final Collection<UUID> markerIds, final boolean exclude) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(markerIds)) return cb.conjunction();
            Join<TwinEntity, TwinMarkerEntity> markerJoin = root.join(TwinEntity.Fields.markers, JoinType.LEFT);
            Predicate markerIdIn = markerJoin.get(TwinMarkerEntity.Fields.markerDataListOptionId).in(markerIds);
            query.distinct(true);
            if (exclude) {
                Predicate noMarkers = cb.isNull(markerJoin.get(TwinMarkerEntity.Fields.twinId));
                return cb.or(noMarkers, cb.not(markerIdIn));
            } else {
                return cb.and(cb.isNotNull(markerJoin.get(TwinMarkerEntity.Fields.twinId)), markerIdIn);
            }
        };
    }

    public static Specification<TwinEntity> checkTouchIds(final Collection<TwinTouchEntity.Touch> touchIds, final UUID userId, final boolean exclude) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(touchIds)) return cb.conjunction();

            Join<TwinEntity, TwinTouchEntity> touchJoin = root.join(TwinEntity.Fields.touches, JoinType.LEFT);

            Predicate onUserId = cb.equal(touchJoin.get(TwinTouchEntity.Fields.userId), userId);
            Predicate onTouchId = touchJoin.get(TwinTouchEntity.Fields.touchId).in(touchIds);
            touchJoin.on(cb.and(onUserId, onTouchId));

            Predicate touchIsNull = cb.isNull(touchJoin.get(TwinTouchEntity.Fields.touchId));
            if (exclude)
                return cb.and(touchIsNull);
            else
                return cb.and(cb.not(touchIsNull));
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

    public static Specification<TwinEntity> checkUuidIn(final String uuidField, final Collection<UUID> uuids, boolean not, boolean ifNotIsTrueIncludeNullValues) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return not ?
                    (ifNotIsTrueIncludeNullValues ? cb.or(root.get(uuidField).in(uuids).not(), root.get(uuidField).isNull()) : root.get(uuidField).in(uuids).not())
                    : root.get(uuidField).in(uuids);
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

    public static Specification<TwinEntity> checkTwinClassUuidFieldIn(final String field, final Collection<UUID> uuids) {
        return (root, query, cb) -> {
            Join<TwinEntity, TwinClassEntity> twinClassJoin = root.join(TwinEntity.Fields.twinClass, JoinType.INNER);
            Predicate predicate = cb.conjunction();
            if (CollectionUtils.isNotEmpty(uuids)) {
                predicate = twinClassJoin.get(field).in(uuids);
            }
            return predicate;
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

    public static Specification<TwinEntity> checkTwinLinks(Map<UUID, Set<UUID>> linksAnyOfList, Map<UUID, Set<UUID>> linksNoAnyOfList, Map<UUID, Set<UUID>> linksAllOfList, Map<UUID, Set<UUID>> linksNoAllOfList) {
        return (root, query, cb) -> {
            List<Predicate> predicatesAny = new ArrayList<>();
            if (MapUtils.isNotEmpty(linksAnyOfList)) {
                Join<TwinEntity, TwinLinkEntity> linkSrcTwinInnerJoin = root.join(TwinEntity.Fields.linksBySrcTwinId, JoinType.INNER);
                for (Map.Entry<UUID, Set<UUID>> entry : linksAnyOfList.entrySet()) {
                    Predicate linkCondition = cb.equal(linkSrcTwinInnerJoin.get(TwinLinkEntity.Fields.linkId), entry.getKey());
                    Predicate dstTwinCondition = entry.getValue().isEmpty() ? cb.conjunction() : linkSrcTwinInnerJoin.get(TwinLinkEntity.Fields.dstTwinId).in(entry.getValue());
                    predicatesAny.add(cb.and(linkCondition, dstTwinCondition));
                }
            }
            List<Predicate> predicatesAll = new ArrayList<>();
            if (MapUtils.isNotEmpty(linksAllOfList)) {
                for (Map.Entry<UUID, Set<UUID>> entry : linksAllOfList.entrySet()) {
                    Join<TwinEntity, TwinLinkEntity> linkSrcTwinInner = root.join(TwinEntity.Fields.linksBySrcTwinId, JoinType.INNER);
                    Predicate dstTwinCondition = entry.getValue().isEmpty() ? cb.conjunction() : linkSrcTwinInner.get(TwinLinkEntity.Fields.dstTwinId).in(entry.getValue());
                    linkSrcTwinInner.on(dstTwinCondition);
                    Predicate linkCondition = cb.equal(linkSrcTwinInner.get(TwinLinkEntity.Fields.linkId), entry.getKey());
                    predicatesAll.add(cb.and(linkCondition));
                }
            }

            Predicate include;
            if (!predicatesAny.isEmpty() && !predicatesAll.isEmpty())
                include = cb.and(cb.or(predicatesAny.toArray(new Predicate[0])), cb.and(predicatesAll.toArray(new Predicate[0])));
            else if (!predicatesAny.isEmpty())
                include = cb.or(predicatesAny.toArray(new Predicate[0]));
            else if (!predicatesAll.isEmpty())
                include = cb.and(predicatesAll.toArray(new Predicate[0]));
            else
                include = cb.conjunction();


            List<Predicate> excludePredicatesAny = new ArrayList<>();
            if (MapUtils.isNotEmpty(linksNoAnyOfList)) {
                for (Map.Entry<UUID, Set<UUID>> entry : linksNoAnyOfList.entrySet()) {
                    Join<TwinEntity, TwinLinkEntity> linkSrcTwinInnerJoin = root.join(TwinEntity.Fields.linksBySrcTwinId, JoinType.LEFT);
                    Predicate onLink = cb.equal(linkSrcTwinInnerJoin.get(TwinLinkEntity.Fields.linkId), entry.getKey());
                    Predicate onDst = entry.getValue().isEmpty() ? cb.conjunction() : linkSrcTwinInnerJoin.get(TwinLinkEntity.Fields.dstTwinId).in(entry.getValue());
                    linkSrcTwinInnerJoin.on(onLink, onDst);
                    excludePredicatesAny.add(cb.isNull(linkSrcTwinInnerJoin.get(TwinLinkEntity.Fields.srcTwinId)));
                }
            }

            List<Predicate> excludePredicatesAll = new ArrayList<>();
            if (MapUtils.isNotEmpty(linksNoAllOfList)) {
                for (Map.Entry<UUID, Set<UUID>> entry : linksNoAllOfList.entrySet()) {
                    Join<TwinEntity, TwinLinkEntity> linkSrcTwinInnerJoin = root.join(TwinEntity.Fields.linksBySrcTwinId, JoinType.LEFT);
                    Predicate onLink = cb.equal(linkSrcTwinInnerJoin.get(TwinLinkEntity.Fields.linkId), entry.getKey());
                    Predicate onDst = entry.getValue().isEmpty() ? cb.conjunction() : linkSrcTwinInnerJoin.get(TwinLinkEntity.Fields.dstTwinId).in(entry.getValue());
                    linkSrcTwinInnerJoin.on(onLink, onDst);
                    excludePredicatesAll.add(cb.isNull(linkSrcTwinInnerJoin.get(TwinLinkEntity.Fields.srcTwinId)));
                }
            }

            Predicate exclude;
            if (!excludePredicatesAny.isEmpty() && !excludePredicatesAll.isEmpty())
                exclude = cb.and(cb.or(excludePredicatesAny.toArray(new Predicate[0])), cb.and(excludePredicatesAll.toArray(new Predicate[0])));
            else if (!excludePredicatesAny.isEmpty())
                exclude = cb.or(excludePredicatesAny.toArray(new Predicate[0]));
            else if (!excludePredicatesAll.isEmpty())
                exclude = cb.and(excludePredicatesAll.toArray(new Predicate[0]));
            else
                exclude = cb.conjunction();

            return cb.and(include, exclude);
        };
    }

    public static Specification<TwinEntity> checkFieldNumeric(final TwinFieldSearchNumeric search) throws ServiceException {
        return (root, query, cb) -> {
            Join<TwinEntity, TwinFieldSimpleEntity> twinFieldSimpleJoin = root.join(TwinEntity.Fields.fieldsSimple, JoinType.INNER);
            twinFieldSimpleJoin.on(cb.equal(twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()));
            // convert string to double in DB for math compare
            Expression<Double> numericValue = twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.value).as(Double.class);
            List<Predicate> predicates = new ArrayList<>();
            if (search.getLessThen() != null)
                predicates.add(cb.lessThan(numericValue, cb.literal(search.getLessThen())));

            if (search.getMoreThen() != null)
                predicates.add(cb.greaterThan(numericValue, cb.literal(search.getMoreThen())));

            Predicate lessAndMore = null;
            if (!predicates.isEmpty())
                lessAndMore = getPredicate(cb, predicates, false);

            Predicate equals = null;
            if (search.getEquals() != null)
                equals = cb.equal(numericValue, cb.literal(search.getEquals()));

            Predicate finalPredicate = cb.conjunction();
            if (null != equals && null != lessAndMore) {
                predicates = new ArrayList<>();
                predicates.add(lessAndMore);
                predicates.add(equals);
                finalPredicate = getPredicate(cb, predicates, true);
            } else if (null != equals)
                finalPredicate = equals;
            else if (null != lessAndMore)
                finalPredicate = lessAndMore;
            return finalPredicate;
        };
    }

    public static Specification<TwinEntity> checkFieldDate(final TwinFieldSearchDate search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<TwinEntity, TwinFieldSimpleEntity> twinFieldSimpleJoin = root.join(TwinEntity.Fields.fieldsSimple, JoinType.INNER);
            twinFieldSimpleJoin.on(cb.equal(twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()));
            Expression<String> stringValue = twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.value);
            Expression<LocalDateTime> dateTimeValue = stringValue.as(LocalDateTime.class);

            Predicate lessAndMore = null;
            Predicate equals = null;
            if(search.getLessThen() != null || search.getMoreThen() != null || search.getEquals() != null) {
                predicates.add(cb.and(cb.isNotNull(stringValue), cb.notEqual(stringValue, cb.literal(""))));
                if (search.getLessThen() != null)
                    predicates.add(cb.and(cb.lessThan(dateTimeValue, cb.literal(search.getLessThen()))));
                if (search.getMoreThen() != null)
                    predicates.add(cb.and(cb.greaterThan(dateTimeValue, cb.literal(search.getMoreThen()))));
                lessAndMore = getPredicate(cb, predicates, false);
                if (search.getEquals() != null)
                    equals = cb.and(cb.equal(dateTimeValue, cb.literal(search.getEquals())));
            }

            Predicate valuePredicate;
            Predicate finalPredicate = cb.conjunction();
            if (null != equals && null != lessAndMore) {
                predicates = new ArrayList<>();
                predicates.add(lessAndMore);
                predicates.add(equals);
                valuePredicate = getPredicate(cb, predicates, true);
            } else if (null != equals)
                valuePredicate = equals;
            else if (null != lessAndMore)
                valuePredicate = lessAndMore;
            else
                valuePredicate = search.isEmpty() ? cb.disjunction() : cb.conjunction();

            if(search.isEmpty())
                finalPredicate = cb.or(valuePredicate, cb.or(
                        cb.equal(stringValue, cb.literal("")),
                        cb.isNull(stringValue)
                ));
            else finalPredicate = cb.and(valuePredicate, finalPredicate);
            return finalPredicate;
        };
    }


    public static Specification<TwinEntity> checkFieldText(final TwinFieldSearchText search) {
        return (root, query, cb) -> {
            Join<TwinEntity, TwinFieldSimpleEntity> twinFieldSimpleJoin = root.join(TwinEntity.Fields.fieldsSimple, JoinType.INNER);
            twinFieldSimpleJoin.on(cb.equal(twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()));

            List<Predicate> predicatesAny = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search.getValueLikeAnyOfList()))
                for (String value : search.getValueLikeAnyOfList())
                    predicatesAny.add(cb.like(twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.value), value));
            List<Predicate> predicatesAll = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search.getValueLikeAllOfList()))
                for (String value : search.getValueLikeAllOfList())
                    predicatesAll.add(cb.like(twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.value), value));

            Predicate include;
            if (!predicatesAny.isEmpty() && !predicatesAll.isEmpty())
                include = cb.and(cb.or(predicatesAny.toArray(new Predicate[0])), cb.and(predicatesAll.toArray(new Predicate[0])));
            else if (!predicatesAny.isEmpty())
                include = cb.or(predicatesAny.toArray(new Predicate[0]));
            else if (!predicatesAll.isEmpty())
                include = cb.and(predicatesAll.toArray(new Predicate[0]));
            else
                include = cb.conjunction();


            List<Predicate> excludePredicatesAny = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search.getValueLikeNoAnyOfList()))
                for (String value : search.getValueLikeNoAnyOfList())
                  excludePredicatesAny.add(cb.notLike(twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.value), value));
            List<Predicate> excludePredicatesAll = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search.getValueLikeNoAllOfList()))
                for (String value : search.getValueLikeNoAllOfList())
                    excludePredicatesAll.add(cb.notLike(twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.value), value));

            Predicate exclude;
            if (!excludePredicatesAny.isEmpty() && !excludePredicatesAll.isEmpty())
                exclude = cb.and(cb.or(excludePredicatesAny.toArray(new Predicate[0])), cb.and(excludePredicatesAll.toArray(new Predicate[0])));
            else if (!excludePredicatesAny.isEmpty())
                exclude = cb.or(excludePredicatesAny.toArray(new Predicate[0]));
            else if (!excludePredicatesAll.isEmpty())
                exclude = cb.and(excludePredicatesAll.toArray(new Predicate[0]));
            else
                exclude = cb.conjunction();

            return cb.and(include, exclude);
        };
    }

    public static Specification<TwinEntity> checkFieldList(final TwinFieldSearchList search) {
        return (root, query, cb) -> {
            Join<TwinEntity, TwinFieldDataListEntity> twinFieldListJoin = root.join(TwinEntity.Fields.fieldsList, JoinType.INNER);
            twinFieldListJoin.on(cb.equal(twinFieldListJoin.get(TwinFieldDataListEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()));

            Predicate includeAny = cb.conjunction();
            if (CollectionUtils.isNotEmpty(search.getOptionsAnyOfList()))
                includeAny = twinFieldListJoin.get(TwinFieldDataListEntity.Fields.dataListOptionId).in(search.getOptionsAnyOfList());

            Predicate includeAll = cb.conjunction();
            if (CollectionUtils.isNotEmpty(search.getOptionsAllOfList())) {
                List<Predicate> allOfPredicates = new ArrayList<>();
                for (UUID option : search.getOptionsAllOfList()) {
                    Join<TwinEntity, TwinFieldDataListEntity> twinFieldJoinForAll = root.join(TwinEntity.Fields.fieldsList, JoinType.INNER);
                    twinFieldJoinForAll.on(
                            cb.equal(twinFieldJoinForAll.get(TwinFieldDataListEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()),
                            cb.equal(twinFieldJoinForAll.get(TwinFieldDataListEntity.Fields.dataListOptionId), option)
                    );
                    allOfPredicates.add(cb.isNotNull(twinFieldJoinForAll));
                }
                includeAll = getPredicate(cb, allOfPredicates, false);cb.and(allOfPredicates.toArray(new Predicate[0]));
            }

            Predicate include = cb.and(includeAny, includeAll);


            Predicate excludeAny = cb.conjunction();
            if (CollectionUtils.isNotEmpty(search.getOptionsNoAnyOfList()))
                excludeAny = cb.not(twinFieldListJoin.get(TwinFieldDataListEntity.Fields.dataListOptionId).in(search.getOptionsNoAnyOfList()));


            Predicate excludeAll = cb.conjunction();
            if (CollectionUtils.isNotEmpty(search.getOptionsNoAllOfList())) {
                List<Predicate> noAllOfPredicates = new ArrayList<>();
                for (UUID option : search.getOptionsNoAllOfList()) {
                    Join<TwinEntity, TwinFieldDataListEntity> twinFieldJoinForNoAll = root.join(TwinEntity.Fields.fieldsList, JoinType.LEFT);
                    twinFieldJoinForNoAll.on(
                            cb.equal(twinFieldJoinForNoAll.get(TwinFieldDataListEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()),
                            cb.equal(twinFieldJoinForNoAll.get(TwinFieldDataListEntity.Fields.dataListOptionId), option)
                    );
                    noAllOfPredicates.add(cb.isNotNull(twinFieldJoinForNoAll));
                }
                excludeAll = cb.not(getPredicate(cb, noAllOfPredicates, false));
            }

            Predicate exclude = cb.and(excludeAny, excludeAll);

            return cb.and(include, exclude);

        };
    }
}
