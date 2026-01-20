package org.twins.core.dao.specifications;

import jakarta.persistence.criteria.*;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.LTreeUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twin.*;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.enums.twin.Touch;

import java.util.*;

import static org.cambium.common.util.ArrayUtils.concatArray;
import static org.cambium.common.util.SpecificationUtils.getPredicate;

public abstract class AbstractTwinEntityBasicSearchSpecification<T> extends CommonSpecification<T> {

    public static <T> Specification<T> createTwinEntityBasicSearchSpecification(TwinSearch twinSearch, UUID userId, String... twinsEntityFieldPath) throws ServiceException {

        String[] idFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.id);
        String[] nameFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.name);
        String[] descriptionFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.description);
        String[] externalIdFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.externalId);
        String[] assignerUserIdFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.assignerUserId);
        String[] createdByUserIdFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.createdByUserId);
        String[] twinStatusIdFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.twinStatusId);
        String[] headTwinIdFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.headTwinId);
        String[] hierarchyTreeFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.hierarchyTree);
        String[] twinClassIdFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.twinClassId);
        String[] twinClassFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.twinClass);
        String[] twinClassExtendsHierarchyTreeFieldPath = concatArray(twinClassFieldPath, TwinClassEntity.Fields.extendsHierarchyTree);
        String[] createdAtFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.createdAt);
        String[] tagsFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.tags, TwinTagEntity.Fields.tagDataListOptionId);
        String[] markersFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.markers, TwinMarkerEntity.Fields.markerDataListOptionId);
        String[] touchFieldPath = concatArray(twinsEntityFieldPath, TwinEntity.Fields.touches);

        HierarchySearch hierarchyChildrenSearch = Objects.requireNonNullElse(twinSearch.getHierarchyChildrenSearch(), HierarchySearch.EMPTY);

        var commonSpecifications = new Specification[] {
                checkTwinLinks(twinSearch, false,twinsEntityFieldPath),
                checkTwinLinks(twinSearch, true, twinsEntityFieldPath),
                checkUuidIn(twinSearch.getTwinIdList(), false, false, idFieldPath),
                checkUuidIn(twinSearch.getTwinIdExcludeList(), true, false, idFieldPath),
                checkFieldLikeIn(twinSearch.getTwinNameLikeList(), false, true, nameFieldPath),
                checkFieldLikeIn(twinSearch.getTwinNameNotLikeList(), true, true, nameFieldPath),
                checkFieldLikeIn(twinSearch.getTwinDescriptionLikeList(), false, true, descriptionFieldPath),
                checkFieldLikeIn(twinSearch.getTwinDescriptionNotLikeList(), true, true, descriptionFieldPath),
                checkFieldLikeIn(twinSearch.getExternalIdList(), false, true, externalIdFieldPath),
                checkFieldLikeIn(twinSearch.getExternalIdExcludeList(), true, true, externalIdFieldPath),
                checkUuidIn(twinSearch.getAssigneeUserIdList(), false, false, assignerUserIdFieldPath),
                checkUuidIn(twinSearch.getAssigneeUserIdExcludeList(), true, true, assignerUserIdFieldPath),
                checkUuidIn(twinSearch.getCreatedByUserIdList(), false, false, createdByUserIdFieldPath),
                checkUuidIn(twinSearch.getCreatedByUserIdExcludeList(), true, true, createdByUserIdFieldPath),
                checkUuidIn(twinSearch.getStatusIdList(), false, false, twinStatusIdFieldPath),
                checkUuidIn(twinSearch.getStatusIdExcludeList(), true, false, twinStatusIdFieldPath),
                checkUuidIn(twinSearch.getHeadTwinIdList(), false, false, headTwinIdFieldPath),
                checkUuidIn(twinSearch.getTwinClassIdExcludeList(), true, false, twinClassIdFieldPath),
                checkUuidIn(twinSearch.getTagDataListOptionIdList(), false, false, tagsFieldPath),
                checkUuidIn(twinSearch.getTagDataListOptionIdExcludeList(), true, true, tagsFieldPath),
                checkHierarchyContainsAny(twinSearch.getHierarchyTreeContainsIdList(), hierarchyTreeFieldPath),
                checkUuidIn(twinSearch.getMarkerDataListOptionIdList(), false, false, markersFieldPath),
                checkUuidIn(twinSearch.getMarkerDataListOptionIdExcludeList(), true, true, markersFieldPath),
                checkUuidIn(twinSearch.getHeadTwinClassIdList(), false, false, concatArray(twinClassFieldPath, TwinClassEntity.Fields.headTwinClassId)),
                checkHierarchyContainsAny(twinSearch.getTwinClassExtendsHierarchyContainsIdList(), twinClassExtendsHierarchyTreeFieldPath),
                checkTouchSearch(userId,false,twinSearch.getTouchList(),touchFieldPath),
                checkTouchSearch(userId,true,twinSearch.getTouchExcludeList(),touchFieldPath),
                checkFieldLocalDateTimeBetween(twinSearch.getCreatedAt(), TwinEntity.Fields.createdAt),
                checkHierarchyChildren(hierarchyChildrenSearch.getIdList(), false, false, hierarchyChildrenSearch.getDepth(), hierarchyTreeFieldPath),
                checkQueryDistinct(twinSearch.getDistinct())
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

    protected static <T> Specification<T> checkTouchSearch(UUID userId, boolean exclude, Collection<Touch> touchCollection, String... touchFieldPath) {
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

    protected static Specification checkTwinLinks(TwinSearch twinSearch, boolean srcElseDst, String... twinsEntityFieldPath) {
        return (root, query, cb) -> {

            From twinsJoin = getReducedRoot(root, JoinType.INNER, twinsEntityFieldPath);

            String targetJoinFieldName = srcElseDst ? TwinEntity.Fields.linksByDstTwinId : TwinEntity.Fields.linksBySrcTwinId;
            String searchDirectionFieldName = srcElseDst ? TwinLinkEntity.Fields.srcTwinId : TwinLinkEntity.Fields.dstTwinId;
            Map<UUID, Set<UUID>> linksAnyOfList = srcElseDst ? twinSearch.getSrcLinksAnyOfList() : twinSearch.getDstLinksAnyOfList();
            Map<UUID, Set<UUID>> linksAllOfList = srcElseDst ? twinSearch.getSrcLinksAllOfList() : twinSearch.getDstLinksAllOfList();
            Map<UUID, Set<UUID>> linksNoAnyOfList = srcElseDst ? twinSearch.getSrcLinksNoAnyOfList() : twinSearch.getDstLinksNoAnyOfList();
            Map<UUID, Set<UUID>> linksNoAllOfList = srcElseDst ? twinSearch.getSrcLinksNoAllOfList() : twinSearch.getDstLinksNoAllOfList();

            List<Predicate> predicatesAny = new ArrayList<>();
            if (MapUtils.isNotEmpty(linksAnyOfList)) {
                Join<TwinEntity, TwinLinkEntity> linkTwinInnerJoin = twinsJoin.join(targetJoinFieldName, JoinType.INNER);
                for (Map.Entry<UUID, Set<UUID>> entry : linksAnyOfList.entrySet()) {
                    Predicate linkCondition = cb.equal(linkTwinInnerJoin.get(TwinLinkEntity.Fields.linkId), entry.getKey());
                    Predicate searchDirectionTwinCondition = entry.getValue().isEmpty() ? cb.conjunction() : linkTwinInnerJoin.get(searchDirectionFieldName).in(entry.getValue());
                    predicatesAny.add(cb.and(linkCondition, searchDirectionTwinCondition));
                }
            }

            List<Predicate> predicatesAll = new ArrayList<>();
            if (MapUtils.isNotEmpty(linksAllOfList)) {
                for (Map.Entry<UUID, Set<UUID>> entry : linksAllOfList.entrySet()) {
                    Join linkTwinInner = twinsJoin.join(targetJoinFieldName, JoinType.INNER);
                    Predicate searchDirectionTwinCondition = entry.getValue().isEmpty() ? cb.conjunction() : linkTwinInner.get(searchDirectionFieldName).in(entry.getValue());
                    linkTwinInner.on(searchDirectionTwinCondition);
                    Predicate linkCondition = cb.equal(linkTwinInner.get(TwinLinkEntity.Fields.linkId), entry.getKey());
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
                    Join linkTwinInnerJoin = twinsJoin.join(targetJoinFieldName, JoinType.LEFT);
                    Predicate onLink = cb.equal(linkTwinInnerJoin.get(TwinLinkEntity.Fields.linkId), entry.getKey());
                    Predicate onDirection = entry.getValue().isEmpty() ? cb.conjunction() : linkTwinInnerJoin.get(searchDirectionFieldName).in(entry.getValue());
                    linkTwinInnerJoin.on(onLink, onDirection);
                    excludePredicatesAny.add(cb.isNull(linkTwinInnerJoin.get(srcElseDst ? TwinLinkEntity.Fields.dstTwinId : TwinLinkEntity.Fields.srcTwinId)));
                }
            }

            List<Predicate> excludePredicatesAll = new ArrayList<>();
            if (MapUtils.isNotEmpty(linksNoAllOfList)) {
                for (Map.Entry<UUID, Set<UUID>> entry : linksNoAllOfList.entrySet()) {
                    Join linkTwinInnerJoin = twinsJoin.join(targetJoinFieldName, JoinType.LEFT);
                    Predicate onLink = cb.equal(linkTwinInnerJoin.get(TwinLinkEntity.Fields.linkId), entry.getKey());
                    Predicate onDirection = entry.getValue().isEmpty() ? cb.conjunction() : linkTwinInnerJoin.get(searchDirectionFieldName).in(entry.getValue());
                    linkTwinInnerJoin.on(onLink, onDirection);
                    excludePredicatesAll.add(cb.isNull(linkTwinInnerJoin.get(srcElseDst ? TwinLinkEntity.Fields.dstTwinId : TwinLinkEntity.Fields.srcTwinId)));
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
}
