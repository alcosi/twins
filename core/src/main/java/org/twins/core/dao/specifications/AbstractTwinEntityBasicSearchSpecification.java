package org.twins.core.dao.specifications;

import io.hypersistence.utils.hibernate.type.array.UUIDArrayType;
import jakarta.persistence.criteria.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Range;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ArrayUtils;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.LTreeUtils;
import org.hibernate.query.TypedParameterValue;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twin.*;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.domain.search.TwinSearch;

import java.util.*;
import java.util.stream.Collectors;

import static org.cambium.common.util.ArrayUtils.concatArray;
import static org.cambium.common.util.SpecificationUtils.getPredicate;

public abstract class AbstractTwinEntityBasicSearchSpecification<T> extends CommonSpecification<T> {

    public static <T> Specification<T> createTwinEntityBasicSearchSpecification(TwinSearch twinSearch, UUID userId, String... twinsEntityFieldPath) throws ServiceException {

        String[] idFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.id);
        String[] nameFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.name);
        String[] descriptionFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.description);
        String[] assignerUserIdFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.assignerUserId);
        String[] createdByUserIdFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.createdByUserId);
        String[] twinStatusIdFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.twinStatusId);
        String[] headTwinIdFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.headTwinId);
        String[] hierarchyTreeFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.hierarchyTree);
        String[] twinClassIdFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.twinClassId);
        String[] twinClassFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.twinClass);

        String[] tagsFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.tags, TwinTagEntity.Fields.tagDataListOptionId);
        String[] markersFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.markers, TwinMarkerEntity.Fields.markerDataListOptionId);
        String[] touchFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.touches);

        var commonSpecifications = new Specification[]{
                checkTwinLinks(twinSearch, twinsEntityFieldPath),
                checkUuidIn(twinSearch.getTwinIdList(), false, false, idFieldPath),
                checkUuidIn(twinSearch.getTwinIdExcludeList(), true, false, idFieldPath),
                checkFieldLikeIn(twinSearch.getTwinNameLikeList(), false, true, nameFieldPath),
                checkFieldLikeIn(twinSearch.getTwinNameNotLikeList(), true, true, nameFieldPath),
                checkFieldLikeIn(twinSearch.getTwinDescriptionLikeList(), false, true, descriptionFieldPath),
                checkFieldLikeIn(twinSearch.getTwinDescriptionNotLikeList(), true, true, descriptionFieldPath),
                checkUuidIn(twinSearch.getAssigneeUserIdList(), false, false, assignerUserIdFieldPath),
                checkUuidIn(twinSearch.getAssigneeUserIdExcludeList(), true, true, assignerUserIdFieldPath),
                checkUuidIn(twinSearch.getCreatedByUserIdList(), false, false, createdByUserIdFieldPath),
                checkUuidIn(twinSearch.getCreatedByUserIdExcludeList(), true, true, createdByUserIdFieldPath),
                checkUuidIn(twinSearch.getStatusIdList(), false, false, twinStatusIdFieldPath),
                checkUuidIn(twinSearch.getStatusIdExcludeList(), true, false, twinStatusIdFieldPath),
                checkUuidIn(twinSearch.getHeaderTwinIdList(), false, false, headTwinIdFieldPath),
                checkUuidIn(twinSearch.getTwinClassIdExcludeList(), true, false, twinClassIdFieldPath),
                checkUuidIn(twinSearch.getTagDataListOptionIdList(), false, false, tagsFieldPath),
                checkUuidIn(twinSearch.getTagDataListOptionIdExcludeList(), true, true, tagsFieldPath),
                checkHierarchyContainsAny(twinSearch.getHierarchyTreeContainsIdList(), hierarchyTreeFieldPath),
                checkUuidIn(twinSearch.getMarkerDataListOptionIdList(), false, false, markersFieldPath),
                checkUuidIn(twinSearch.getMarkerDataListOptionIdExcludeList(), true, true, markersFieldPath),
                checkUuidIn(twinSearch.getHeadTwinClassIdList(), false, false, concatArray(twinClassFieldPath, TwinClassEntity.Fields.headTwinClassId)),
                checkUuidIn(twinSearch.getExtendsTwinClassIdList(), false, false, concatArray(twinClassFieldPath, TwinClassEntity.Fields.extendsTwinClassId)),
                checkTouchSearch(userId,false,twinSearch.getTouchList(),touchFieldPath),
                checkTouchSearch(userId,true,twinSearch.getTouchExcludeList(),touchFieldPath),
        };

        return Specification.allOf(concatArray(commonSpecifications, getTwinSearchFieldsSpecifications(twinSearch.getFields())));
    }

    protected static <T> Specification<T> checkHierarchyContainsAny(final Set<UUID> hierarchyTreeContainsIdList, String... hierarchyFieldPath) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(hierarchyTreeContainsIdList)) return cb.conjunction();
            List<Predicate> predicates = hierarchyTreeContainsIdList.stream().map(id -> {
                Path hierarchyTreeExpression = getFieldPath(root, JoinType.INNER, hierarchyFieldPath);
                return cb.isTrue(cb.function("hierarchy_check_lquery", Boolean.class, hierarchyTreeExpression, cb.literal(LTreeUtils.matchInTheMiddle(id))));

            }).toList();
            return getPredicate(cb, predicates, true);
        };
    }

    protected static <T> Specification<T> checkTouchSearch(UUID userId, boolean exclude, Collection<TwinTouchEntity.Touch> touchCollection, String... touchFieldPath) {
        return (root, q, cb) -> {
            if (touchCollection == null || touchCollection.isEmpty()) {
                return cb.conjunction();
            }
            Join touchJoin = (Join) getReducedRoot(root, JoinType.LEFT, touchFieldPath);
            Predicate onUserId = cb.equal(touchJoin.get(TwinTouchEntity.Fields.userId), userId);
            Predicate onTouchId = touchJoin.get(TwinTouchEntity.Fields.touchId).in(touchCollection);
            touchJoin.on(cb.and(onUserId, onTouchId));
            Predicate touchIsNull = cb.isNull(touchJoin.get(TwinTouchEntity.Fields.touchId));
            return exclude ? cb.and(touchIsNull) : cb.and(cb.not(touchIsNull));
        };
    }

    protected static Specification[] getTwinSearchFieldsSpecifications(List<TwinFieldSearch> fields) {
        if (fields == null || fields.isEmpty()) {
            return new Specification[0];
        }
        Specification[] twinSearchFieldsSpecifications = fields.stream().map(fieldSearch -> {
            try {
                return fieldSearch.getFieldTyper().searchBy(fieldSearch);
            } catch (ServiceException e) {
                throw new RuntimeException(e);
            }
        }).toArray(Specification[]::new);
        return twinSearchFieldsSpecifications;
    }

    protected static Specification checkTwinLinks(TwinSearch twinSearch, String... twinsEntityFieldPath) {
        return (root, query, cb) -> {
            From twinsJoin = getReducedRoot(root, JoinType.INNER, twinsEntityFieldPath);
            List<Predicate> predicatesAny = new ArrayList<>();
            if (MapUtils.isNotEmpty(twinSearch.getLinksAnyOfList())) {
                Join linkSrcTwinInnerJoin = twinsJoin.join(TwinEntity.Fields.linksBySrcTwinId, JoinType.INNER);
                for (Map.Entry<UUID, Set<UUID>> entry : twinSearch.getLinksAnyOfList().entrySet()) {
                    Predicate linkCondition = cb.equal(linkSrcTwinInnerJoin.get(TwinLinkEntity.Fields.linkId), entry.getKey());
                    Predicate dstTwinCondition = entry.getValue().isEmpty() ? cb.conjunction() : linkSrcTwinInnerJoin.get(TwinLinkEntity.Fields.dstTwinId).in(entry.getValue());
                    predicatesAny.add(cb.and(linkCondition, dstTwinCondition));
                }
            }
            List<Predicate> predicatesAll = new ArrayList<>();
            if (MapUtils.isNotEmpty(twinSearch.getLinksAllOfList())) {
                for (Map.Entry<UUID, Set<UUID>> entry : twinSearch.getLinksAllOfList().entrySet()) {
                    Join linkSrcTwinInner = twinsJoin.join(TwinEntity.Fields.linksBySrcTwinId, JoinType.INNER);
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
            if (MapUtils.isNotEmpty(twinSearch.getLinksNoAnyOfList())) {
                for (Map.Entry<UUID, Set<UUID>> entry : twinSearch.getLinksNoAnyOfList().entrySet()) {
                    Join linkSrcTwinInnerJoin = twinsJoin.join(TwinEntity.Fields.linksBySrcTwinId, JoinType.LEFT);
                    Predicate onLink = cb.equal(linkSrcTwinInnerJoin.get(TwinLinkEntity.Fields.linkId), entry.getKey());
                    Predicate onDst = entry.getValue().isEmpty() ? cb.conjunction() : linkSrcTwinInnerJoin.get(TwinLinkEntity.Fields.dstTwinId).in(entry.getValue());
                    linkSrcTwinInnerJoin.on(onLink, onDst);
                    excludePredicatesAny.add(cb.isNull(linkSrcTwinInnerJoin.get(TwinLinkEntity.Fields.srcTwinId)));
                }
            }

            List<Predicate> excludePredicatesAll = new ArrayList<>();
            if (MapUtils.isNotEmpty(twinSearch.getLinksNoAllOfList())) {
                for (Map.Entry<UUID, Set<UUID>> entry : twinSearch.getLinksNoAllOfList().entrySet()) {
                    Join linkSrcTwinInnerJoin = twinsJoin.join(TwinEntity.Fields.linksBySrcTwinId, JoinType.LEFT);
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


    public static <T> Specification<T> checkExtendsTwinClassChilds(Collection<UUID> ids, boolean not,
                                                                   boolean includeNullValues, Integer depthLimit, final String... twinClassFieldPath) {
        return checkLtreeChilds(ids, not, includeNullValues, depthLimit, ArrayUtils.concatArray(twinClassFieldPath, TwinClassEntity.Fields.extendsHierarchyTree));
    }

    public static <T> Specification<T> checkHeadTwinClassChilds(Collection<UUID> ids, boolean not,
                                                                boolean includeNullValues, Integer depthLimit, final String... twinClassFieldPath) {
        return checkLtreeChilds(ids, not, includeNullValues, depthLimit, ArrayUtils.concatArray(twinClassFieldPath, TwinClassEntity.Fields.headHierarchyTree));
    }

    public static <T> Specification<T> checkLtreeChilds(Collection<UUID> ids, boolean not,
                                                        boolean includeNullValues, Integer depthLimit, final String... ltreeFieldPath) {

        return (root, query, cb) -> {
            if (org.cambium.common.util.CollectionUtils.isEmpty(ids))
                return cb.conjunction();
            var preparedDepthLimit = depthLimit == null ? 1 : depthLimit;
            var preparedIds = LTreeUtils.findChildsLQuery(ids.stream().map(UUID::toString).collect(Collectors.toList()), Range.of(0, preparedDepthLimit));
            Path<String> ltreePath = getFieldPath(root, includeNullValues ? JoinType.LEFT : JoinType.INNER, ltreeFieldPath);
            Predicate idPredicate;
            var ltreeIsInFunction = cb.function("hierarchy_check_lquery", Boolean.class, ltreePath, cb.literal(preparedIds));
            if (not) {
                idPredicate = cb.isFalse(ltreeIsInFunction);
            } else {
                idPredicate = cb.isTrue(ltreeIsInFunction);
            }
            if (includeNullValues) {
                return cb.or(idPredicate, ltreePath.isNull());
            } else {
                return idPredicate;
            }
        };
    }

    public static <T> Specification<T> checkExtendsTwinClassParents(Collection<UUID> ids, boolean not,
                                                                    boolean includeNullValues, Integer depthLimit, final String... twinClassFieldPath) {
        return checkTwinClassParent(ids, not, includeNullValues, depthLimit, twinClassFieldPath, "ltree_get_extends_parent_uuid");
    }

    public static <T> Specification<T> checkHeadTwinClassParents(Collection<UUID> ids, boolean not,
                                                                 boolean includeNullValues, Integer depthLimit, final String... twinClassFieldPath) {
        return checkTwinClassParent(ids, not, includeNullValues, depthLimit, twinClassFieldPath, "ltree_get_extends_parent_uuid");
    }

    protected static <T> @NotNull Specification<T> checkTwinClassParent(Collection<UUID> ids, boolean not, boolean includeNullValues, Integer depthLimit, String[] ltreeFieldPath, String functionName) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(ids))
                return cb.conjunction();
            var preparedDepthLimit = depthLimit == null ? 1 : depthLimit;
            var typedIds = new TypedParameterValue(UUIDArrayType.INSTANCE, ids);
            Path<String> ltreePath = getFieldPath(root, includeNullValues ? JoinType.LEFT : JoinType.INNER, ltreeFieldPath);
            var ltreeIsInFunction = cb.function(functionName, UUID.class, cb.literal(typedIds), cb.literal(preparedDepthLimit));
            Predicate idPredicate;
            if (not) {
                idPredicate = cb.in(ltreeIsInFunction);
            } else {
                idPredicate = cb.in(ltreeIsInFunction);
            }
            if (includeNullValues) {
                return cb.or(idPredicate, ltreePath.isNull());
            } else {
                return idPredicate;
            }
        };
    }



}
