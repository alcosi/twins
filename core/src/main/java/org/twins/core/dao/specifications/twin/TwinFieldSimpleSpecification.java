package org.twins.core.dao.specifications.twin;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.LTreeUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twin.*;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.TwinSearch;

import java.util.*;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

@Slf4j
public class TwinFieldSimpleSpecification {

    private TwinFieldSimpleSpecification() {
    }

    public static Specification<TwinFieldSimpleEntity> checkTwin(TwinSearch twinSearch, UUID currentUserId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (null != twinSearch) {
                Join<TwinFieldSimpleEntity, ?> twinJoin = getOrCreateJoin(root);
                List<Predicate> classPredicates = new ArrayList<>();
                Predicate classPredicate = null;
                if (!CollectionUtils.isEmpty(twinSearch.getTwinClassIdList())) {
                    for (UUID twinClassId : twinSearch.getTwinClassIdList())
                        classPredicates.add(cb.equal(twinJoin.get(TwinEntity.Fields.twinClassId), twinClassId));
                    if (!classPredicates.isEmpty()) classPredicate = getPredicate(cb, classPredicates, true);
                }
                if (null != classPredicate) predicates.add(classPredicate);
                predicates.add(checkUuidIn(TwinEntity.Fields.id, twinSearch.getTwinIdList(), false, false, root).toPredicate(root, query, cb));
                predicates.add(checkUuidIn(TwinEntity.Fields.id, twinSearch.getTwinIdExcludeList(), true, false, root).toPredicate(root, query, cb));
                predicates.add(checkFieldLikeIn(TwinEntity.Fields.name, twinSearch.getTwinNameLikeList(), false, true, root).toPredicate(root, query, cb));
                predicates.add(checkFieldLikeIn(TwinEntity.Fields.name, twinSearch.getTwinNameNotLikeList(), true, true, root).toPredicate(root, query, cb));
                predicates.add(checkFieldLikeIn(TwinEntity.Fields.description, twinSearch.getTwinDescriptionLikeList(), false, true, root).toPredicate(root, query, cb));
                predicates.add(checkFieldLikeIn(TwinEntity.Fields.description, twinSearch.getTwinDescriptionNotLikeList(), true, true, root).toPredicate(root, query, cb));
                predicates.add(checkUuidIn(TwinEntity.Fields.assignerUserId, twinSearch.getAssigneeUserIdList(), false, false, root).toPredicate(root, query, cb));
                predicates.add(checkUuidIn(TwinEntity.Fields.assignerUserId, twinSearch.getAssigneeUserIdExcludeList(), true, true, root).toPredicate(root, query, cb));
                predicates.add(checkUuidIn(TwinEntity.Fields.createdByUserId, twinSearch.getCreatedByUserIdList(), false, false, root).toPredicate(root, query, cb));
                predicates.add(checkUuidIn(TwinEntity.Fields.createdByUserId, twinSearch.getCreatedByUserIdExcludeList(), true, true, root).toPredicate(root, query, cb));
                predicates.add(checkUuidIn(TwinEntity.Fields.twinStatusId, twinSearch.getStatusIdList(), false, false, root).toPredicate(root, query, cb));
                predicates.add(checkUuidIn(TwinEntity.Fields.headTwinId, twinSearch.getHeaderTwinIdList(), false, false, root).toPredicate(root, query, cb));
                predicates.add(checkTwinHierarchyContainsAny(TwinEntity.Fields.hierarchyTree, twinSearch.getHierarchyTreeContainsIdList(), root).toPredicate(root, query, cb));
                predicates.add(checkUuidIn(TwinEntity.Fields.twinStatusId, twinSearch.getStatusIdExcludeList(), true, false, root).toPredicate(root, query, cb));
                predicates.add(checkUuidIn(TwinEntity.Fields.twinClassId, twinSearch.getTwinClassIdExcludeList(), true, false, root).toPredicate(root, query, cb));
                predicates.add(checkTagIds(twinSearch.getTagDataListOptionIdList(), false, root).toPredicate(root, query, cb));
                predicates.add(checkTagIds(twinSearch.getTagDataListOptionIdExcludeList(), true, root).toPredicate(root, query, cb));
                predicates.add(checkMarkerIds(twinSearch.getMarkerDataListOptionIdList(), false, root).toPredicate(root, query, cb));
                predicates.add(checkMarkerIds(twinSearch.getMarkerDataListOptionIdExcludeList(), true, root).toPredicate(root, query, cb));
                predicates.add(checkTouchIds(twinSearch.getTouchList(), currentUserId, false, root).toPredicate(root, query, cb));
                predicates.add(checkTouchIds(twinSearch.getTouchExcludeList(), currentUserId, true, root).toPredicate(root, query, cb));
                predicates.add(checkTwinClassUuidFieldIn(TwinClassEntity.Fields.headTwinClassId, twinSearch.getHeadTwinClassIdList(), root).toPredicate(root, query, cb));
                predicates.add(checkTwinClassUuidFieldIn(TwinClassEntity.Fields.extendsTwinClassId, twinSearch.getExtendsTwinClassIdList(), root).toPredicate(root, query, cb));
                //TODO what about children and head search?
            }
            return predicates.isEmpty() ? cb.conjunction() : getPredicate(cb, predicates, false);

        };
    }

    public static Specification<TwinFieldSimpleEntity> checkTwinClassUuidFieldIn(final String field, final Collection<UUID> uuids, Root<TwinFieldSimpleEntity> mainRoot) {
        return (root, query, cb) -> {
            Join<TwinFieldSimpleEntity, ?> twinJoin = getOrCreateJoin(mainRoot);
            Join<TwinEntity, TwinClassEntity> twinClassJoin = twinJoin.join(TwinEntity.Fields.twinClass, JoinType.INNER);
            Predicate predicate = cb.conjunction();
            if (CollectionUtils.isNotEmpty(uuids)) {
                predicate = twinClassJoin.get(field).in(uuids);
            }
            return predicate;
        };
    }

    public static Specification<TwinFieldSimpleEntity> checkUuidIn(final String uuidField, final Collection<UUID> uuids, boolean not, boolean ifNotIsTrueIncludeNullValues, Root<TwinFieldSimpleEntity> mainRoot) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            Join<TwinFieldSimpleEntity, ?> twinJoin = getOrCreateJoin(mainRoot);
            return not ?
                    (ifNotIsTrueIncludeNullValues ?
                            cb.or(twinJoin.get(uuidField).in(uuids).not(), twinJoin.get(uuidField).isNull())
                            : twinJoin.get(uuidField).in(uuids).not())
                    : twinJoin.get(uuidField).in(uuids);
        };
    }

    public static Specification<TwinFieldSimpleEntity> checkFieldLikeIn(final String field, final Collection<String> search, final boolean not, final boolean or, Root<TwinFieldSimpleEntity> mainRoot) {
        return (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search)) {
                Join<TwinFieldSimpleEntity, ?> twinJoin = getOrCreateJoin(mainRoot);
                for (String s : search) {
                    Predicate predicate = cb.like(cb.lower(twinJoin.get(field)), s.toLowerCase());
                    if (not) predicate = cb.not(predicate);
                    predicates.add(predicate);
                }
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<TwinFieldSimpleEntity> checkTwinHierarchyContainsAny(String field, final Set<UUID> hierarchyTreeContainsIdList, Root<TwinFieldSimpleEntity> mainRoot) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(hierarchyTreeContainsIdList)) return cb.conjunction();
            Join<TwinFieldSimpleEntity, ?> twinJoin = getOrCreateJoin(mainRoot);
            List<Predicate> predicates = new ArrayList<>();
            for (UUID id : hierarchyTreeContainsIdList) {
                String ltreeId = LTreeUtils.matchInTheMiddle(id);
                Expression<String> hierarchyTreeExpression = twinJoin.get(field);
                predicates.add(cb.isTrue(cb.function("hierarchy_check_lquery", Boolean.class, hierarchyTreeExpression, cb.literal(ltreeId))));
            }
            return getPredicate(cb, predicates, true);
        };
    }

    public static Specification<TwinFieldSimpleEntity> checkTagIds(final Collection<UUID> tagIds, final boolean exclude, Root<TwinFieldSimpleEntity> mainRoot) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(tagIds)) return cb.conjunction();
            Join<TwinFieldSimpleEntity, ?> twinJoin = getOrCreateJoin(mainRoot);
            Join<TwinEntity, TwinTagEntity> tagJoin = twinJoin.join(TwinEntity.Fields.tags, JoinType.LEFT);
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

    public static Specification<TwinFieldSimpleEntity> checkMarkerIds(final Collection<UUID> markerIds, final boolean exclude, Root<TwinFieldSimpleEntity> mainRoot) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(markerIds)) return cb.conjunction();
            Join<TwinFieldSimpleEntity, ?> twinJoin = getOrCreateJoin(mainRoot);
            Join<TwinEntity, TwinMarkerEntity> markerJoin = twinJoin.join(TwinEntity.Fields.markers, JoinType.LEFT);
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

    public static Specification<TwinFieldSimpleEntity> checkTouchIds(final Collection<TwinTouchEntity.Touch> touchIds, final UUID userId, final boolean exclude, Root<TwinFieldSimpleEntity> mainRoot) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(touchIds)) return cb.conjunction();

            Join<TwinFieldSimpleEntity, ?> twinJoin = getOrCreateJoin(mainRoot);
            Join<TwinEntity, TwinTouchEntity> touchJoin = twinJoin.join(TwinEntity.Fields.touches, JoinType.LEFT);

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

    public static Specification<TwinFieldSimpleEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            if (domainId == null)
                return cb.disjunction();
            Join<TwinFieldSimpleEntity, TwinClassFieldEntity> classFieldjoin = root.join(TwinFieldSimpleEntity.Fields.twinClassField);
            Join<TwinClassFieldEntity, TwinClassEntity> twinClassjoin = classFieldjoin.join(TwinClassFieldEntity.Fields.twinClass);
            return cb.equal(twinClassjoin.get(TwinClassEntity.Fields.domainId), domainId);
        };
    }

    public static Join<TwinFieldSimpleEntity, ?> getOrCreateJoin(Root<TwinFieldSimpleEntity> root) {
        return root.getJoins().stream()
                .filter(j -> j.getAttribute().getName().equals(TwinFieldSimpleEntity.Fields.twin))
                .findFirst()
                .orElseGet(() -> root.join(TwinFieldSimpleEntity.Fields.twin));
    }
}

