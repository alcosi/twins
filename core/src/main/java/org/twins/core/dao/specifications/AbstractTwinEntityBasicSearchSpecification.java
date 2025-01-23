package org.twins.core.dao.specifications;

import jakarta.persistence.criteria.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.LTreeUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinTouchEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.domain.search.TwinSearch;

import java.util.*;
import java.util.function.BiFunction;

import static org.cambium.common.util.ArrayUtils.concatArray;

public abstract class AbstractTwinEntityBasicSearchSpecification<T> extends CommonSpecification<T>{

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
        String[] tagsFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.tags);
        String[] markersFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.markers);
        String[] touchFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.touches);

        var touchSearchFunction = checkTouchSearch(userId, touchFieldPath);
        var hierarchyCheckFunction = checkHierarchyFunction();

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
                (root, q, cb) -> createPredicateWithJoins(root, cb, twinSearch.getHierarchyTreeContainsIdList(), hierarchyCheckFunction, JoinType.INNER, hierarchyTreeFieldPath),
                checkUuidIn(twinSearch.getMarkerDataListOptionIdList(), false, false, markersFieldPath),
                checkUuidIn(twinSearch.getMarkerDataListOptionIdExcludeList(), true, true, markersFieldPath),
                checkUuidIn(twinSearch.getHeadTwinClassIdList(), false, false, TwinEntity.Fields.twinClass, TwinClassEntity.Fields.headTwinClassId),
                checkUuidIn(twinSearch.getExtendsTwinClassIdList(), false, false, TwinEntity.Fields.twinClass, TwinClassEntity.Fields.extendsTwinClassId),
                touchSearchFunction.apply(twinSearch.getTouchList(), false),
                touchSearchFunction.apply(twinSearch.getTouchExcludeList(), true)
        };

        return Specification.allOf(concatArray(commonSpecifications,getTwinSearchFieldsSpecifications(twinSearch.getFields())));
    }

    protected static TriFunction<Path, CriteriaBuilder, Set<UUID>, Predicate> checkHierarchyFunction() {
        return (path, cbi, values) -> {
            Predicate[] predicates = values
                    .stream()
                    .map(id -> cbi.isTrue(cbi.function("hierarchy_check_lquery", Boolean.class, path, cbi.literal(LTreeUtils.matchInTheMiddle(id))))).toArray(Predicate[]::new);
            return cbi.or(predicates);
        };
    }

    protected static BiFunction<Collection<TwinTouchEntity.Touch>, Boolean, Specification> checkTouchSearch(UUID userId, String[] touchFieldPath) {
        return (touchCollection, exclude) -> (root, q, cb) -> {
            Join touchJoin = (Join) getReducedRoot(root, JoinType.LEFT, touchFieldPath);
            Predicate onUserId = cb.equal(touchJoin.get(TwinTouchEntity.Fields.userId), userId);
            Predicate onTouchId = touchJoin.get(TwinTouchEntity.Fields.touchId).in(touchCollection);
            touchJoin.on(cb.and(onUserId, onTouchId));
            Predicate touchIsNull = cb.isNull(touchJoin.get(TwinTouchEntity.Fields.touchId));
            return exclude ? cb.and(touchIsNull) : cb.and(cb.not(touchIsNull));
        };
    }

    protected static Specification[] getTwinSearchFieldsSpecifications(List<TwinFieldSearch> fields) {
        Specification[] twinSearchFieldsSpecifications = fields.stream().map(fieldSearch -> {
            try {
                return fieldSearch.getFieldTyper().searchBy(fieldSearch);
            } catch (ServiceException e) {
                throw new RuntimeException(e);
            }
        }).toArray( Specification[]::new);
        return twinSearchFieldsSpecifications;
    }

    protected static Specification checkTwinLinks(TwinSearch twinSearch, String... twinsEntityFieldPath) {
        Specification checkTwinLinks = (root, query, cb) -> {
            From twinsJoin = getReducedRoot(root, JoinType.INNER, twinsEntityFieldPath);
            List<Predicate> predicatesAny = new ArrayList<>();
            List<Predicate> predicatesAll = new ArrayList<>();
            List<Predicate> excludePredicatesAny = new ArrayList<>();
            List<Predicate> excludePredicatesAll = new ArrayList<>();

            if (MapUtils.isNotEmpty(twinSearch.getLinksAnyOfList())) {
                Join linkSrcTwinInnerJoin = twinsJoin.join(TwinEntity.Fields.linksBySrcTwinId, JoinType.INNER);
                twinSearch.getLinksAnyOfList().forEach((key, value) -> {
                    Predicate linkCondition = cb.equal(linkSrcTwinInnerJoin.get(TwinLinkEntity.Fields.linkId), key);
                    Predicate dstTwinCondition = value.isEmpty() ? cb.conjunction() : linkSrcTwinInnerJoin.get(TwinLinkEntity.Fields.dstTwinId).in(value);
                    predicatesAny.add(cb.and(linkCondition, dstTwinCondition));
                });
            }

            if (MapUtils.isNotEmpty(twinSearch.getLinksAllOfList())) {
                twinSearch.getLinksAllOfList().forEach((key, value) -> {
                    Join linkSrcTwinInner = twinsJoin.join(TwinEntity.Fields.linksBySrcTwinId, JoinType.INNER);
                    Predicate dstTwinCondition = value.isEmpty() ? cb.conjunction() : linkSrcTwinInner.get(TwinLinkEntity.Fields.dstTwinId).in(value);
                    linkSrcTwinInner.on(dstTwinCondition);
                    Predicate linkCondition = cb.equal(linkSrcTwinInner.get(TwinLinkEntity.Fields.linkId), key);
                    predicatesAll.add(cb.and(linkCondition));
                });
            }

            if (MapUtils.isNotEmpty(twinSearch.getLinksNoAnyOfList())) {
                Join linkSrcTwinLeftJoin = twinsJoin.join(TwinEntity.Fields.linksBySrcTwinId, JoinType.LEFT);
                twinSearch.getLinksNoAnyOfList().forEach((key, value) -> {
                    Predicate onLink = cb.equal(linkSrcTwinLeftJoin.get(TwinLinkEntity.Fields.linkId), key);
                    Predicate onDst = value.isEmpty() ? cb.conjunction() : linkSrcTwinLeftJoin.get(TwinLinkEntity.Fields.dstTwinId).in(value);
                    linkSrcTwinLeftJoin.on(onLink, onDst);
                    excludePredicatesAny.add(cb.isNull(linkSrcTwinLeftJoin.get(TwinLinkEntity.Fields.srcTwinId)));
                });
            }

            if (MapUtils.isNotEmpty(twinSearch.getLinksNoAllOfList())) {
                twinSearch.getLinksNoAllOfList().forEach((key, value) -> {
                    Join linkSrcTwinLeftJoin = twinsJoin.join(TwinEntity.Fields.linksBySrcTwinId, JoinType.LEFT);
                    Predicate onLink = cb.equal(linkSrcTwinLeftJoin.get(TwinLinkEntity.Fields.linkId), key);
                    Predicate onDst = value.isEmpty() ? cb.conjunction() : linkSrcTwinLeftJoin.get(TwinLinkEntity.Fields.dstTwinId).in(value);
                    linkSrcTwinLeftJoin.on(onLink, onDst);
                    excludePredicatesAll.add(cb.isNull(linkSrcTwinLeftJoin.get(TwinLinkEntity.Fields.srcTwinId)));
                });
            }

            TriFunction<CriteriaBuilder, List<Predicate>, List<Predicate>, Predicate> getIncludeExcludePredicateFunction = (builder, any, all) -> {
                boolean anyExist = !predicatesAny.isEmpty();
                boolean allExist = !predicatesAll.isEmpty();
                boolean anyAndAllExist = anyExist && allExist;
                return anyAndAllExist ?  builder.and(builder.or(any.toArray(new Predicate[0])), builder.and(all.toArray(new Predicate[0]))):
                        anyExist ? builder.or(any.toArray(new Predicate[0])) :
                                allExist ? builder.and(any.toArray(new Predicate[0])) :
                                        builder.conjunction();
            };
            Predicate include = getIncludeExcludePredicateFunction.apply(cb, predicatesAny, predicatesAll);
            Predicate exclude = getIncludeExcludePredicateFunction.apply(cb, excludePredicatesAny, excludePredicatesAll);
            return cb.and(include, exclude);
        };
        return checkTwinLinks;
    }

}
