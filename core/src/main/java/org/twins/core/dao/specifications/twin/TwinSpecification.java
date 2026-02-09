package org.twins.core.dao.specifications.twin;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.specifications.AbstractTwinEntityBasicSearchSpecification;
import org.twins.core.dao.twin.*;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFreezeEntity;
import org.twins.core.domain.search.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

@Slf4j
public class TwinSpecification extends AbstractTwinEntityBasicSearchSpecification<TwinEntity> {

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

    /**
     * Checks twin status with consideration of freeze status from twin class.
     * If twin class has freeze status (twinClassFreezeId is not null), it has priority over twin's native status.
     *
     * @param statusIdList          list of status ids to check (include)
     * @param statusIdExcludeList   list of status ids to exclude
     * @param checkFreezeStatus     if true, freeze status from twin class will be considered
     * @return specification for status check with freeze consideration
     */
    public static Specification<TwinEntity> checkStatusIdWithFreeze(Set<UUID> statusIdList, Set<UUID> statusIdExcludeList, boolean checkFreezeStatus) {
        return (root, query, cb) -> {
            if (!checkFreezeStatus) {
                // Use standard logic without freeze consideration
                Predicate include = CollectionUtils.isEmpty(statusIdList)
                    ? cb.conjunction()
                    : root.get(TwinEntity.Fields.twinStatusId).in(statusIdList);
                Predicate exclude = CollectionUtils.isEmpty(statusIdExcludeList)
                    ? cb.conjunction()
                    : cb.not(root.get(TwinEntity.Fields.twinStatusId).in(statusIdExcludeList));
                return cb.and(include, exclude);
            }

            // With freeze consideration: use COALESCE(twinClassFreeze.twinStatusId, twin.twinStatusId)
            // Join chain: twin -> twin_class (INNER) -> twin_class_freeze (LEFT)
            Join twinClassJoin = getOrCreateJoin(root, TwinEntity.Fields.twinClass, JoinType.INNER);
            Join twinClassFreezeJoin = getOrCreateJoin(twinClassJoin, TwinClassEntity.Fields.twinClassFreeze, JoinType.LEFT);

            Expression<UUID> effectiveStatus = cb.coalesce(
                twinClassFreezeJoin.get(TwinClassFreezeEntity.Fields.twinStatusId),
                root.get(TwinEntity.Fields.twinStatusId)
            );

            Predicate include = CollectionUtils.isEmpty(statusIdList)
                ? cb.conjunction()
                : effectiveStatus.in(statusIdList);
            Predicate exclude = CollectionUtils.isEmpty(statusIdExcludeList)
                ? cb.conjunction()
                : cb.not(effectiveStatus.in(statusIdExcludeList));
            return cb.and(include, exclude);
        };
    }


    public static Specification<TwinEntity> checkFieldNumeric(final TwinFieldSearchNumeric search) throws ServiceException {
        return (root, query, cb) -> {
            if(search.isEmptySearch()) return cb.conjunction();
            Join<TwinEntity, TwinFieldSimpleEntity> twinFieldSimpleJoin = root.join(TwinEntity.Fields.fieldsSimple, JoinType.INNER);
            twinFieldSimpleJoin.on(cb.equal(twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()));
            // convert string to double in DB for math compare
            Expression<Double> numericValue = cb.function("text2double", Double.class, twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.value));

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

    public static Specification<TwinEntity> checkFieldDate(final TwinFieldSearchDate search, final String... fieldPath) {
        return (root, query, cb) -> {
            if (search.isEmptySearch()) return cb.conjunction();

            Join<TwinEntity, TwinFieldSimpleEntity> twinFieldSimpleJoin = root.join(TwinEntity.Fields.fieldsSimple, JoinType.INNER);
            twinFieldSimpleJoin.on(cb.equal(twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()));

            Expression<String> stringValue = twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.value);
            Expression<LocalDateTime> dateTimeValue = cb.function("text2timestamp", LocalDateTime.class, stringValue);

            Predicate valuePredicate = cb.conjunction();
            if (search.getLessThen() != null || search.getMoreThen() != null || search.getEquals() != null) {
                if(!search.isEmpty())
                    valuePredicate = cb.and(cb.isNotNull(stringValue), cb.notEqual(stringValue, cb.literal("")));

                boolean hasRange = false;
                Predicate rangePredicate = cb.conjunction();
                if (search.getLessThen() != null) {
                    rangePredicate = cb.and(rangePredicate, cb.lessThan(dateTimeValue, cb.literal(search.getLessThen())));
                    hasRange = true;
                }
                if (search.getMoreThen() != null) {
                    rangePredicate = cb.and(rangePredicate, cb.greaterThan(dateTimeValue, cb.literal(search.getMoreThen())));
                    hasRange = true;
                }

                if (search.getEquals() != null) {
                    Predicate equalsPredicate = cb.equal(dateTimeValue, cb.literal(search.getEquals()));
                    if (hasRange) {
                        valuePredicate = cb.and(valuePredicate, cb.or(rangePredicate, equalsPredicate));
                    } else {
                        valuePredicate = cb.and(valuePredicate, equalsPredicate);
                    }
                } else if (hasRange) {
                    valuePredicate = cb.and(valuePredicate, rangePredicate);
                }
            }
            if (search.isEmpty())
                return cb.or(valuePredicate, cb.or(cb.equal(stringValue, cb.literal("")), cb.isNull(stringValue)));

            return valuePredicate;
        };
    }


    public static Specification<TwinEntity> checkFieldUuidIn(final TwinFieldSearchId search, final String... fieldPath) {
        return (root, query, cb) -> {
            Path<UUID> fieldExpression = getFieldPath(root, JoinType.INNER, fieldPath);

            Predicate include;
            if (CollectionUtils.isNotEmpty(search.getIdList())) include = fieldExpression.in(search.getIdList());
            else include = cb.conjunction();

            Predicate exclude;
            if (CollectionUtils.isNotEmpty(search.getIdExcludeList())) exclude = cb.not(fieldExpression.in(search.getIdExcludeList()));
            else exclude = cb.conjunction();

            return cb.and(include, exclude);
        };
    }

    public static Specification<TwinEntity> checkFieldText(final TwinFieldSearchText search, final String... fieldPath) {
        return (root, query, cb) -> {
            if(search.isEmptySearch()) return cb.conjunction();
            Path<String> fieldExpression;
            if (fieldPath.length > 0 && TwinEntity.Fields.fieldsSimple.equals(fieldPath[0])) {
                Join<TwinEntity, TwinFieldSimpleEntity> twinFieldSimpleJoin = root.join(fieldPath[0], JoinType.INNER);
                twinFieldSimpleJoin.on(cb.equal(twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()));
                fieldExpression = twinFieldSimpleJoin.get(fieldPath[1]);
            } else if(fieldPath.length > 0) {
                fieldExpression = getFieldPath(root, JoinType.INNER, fieldPath);
            } else {
                return cb.conjunction();
            }

            List<Predicate> predicatesAny = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search.getValueLikeAnyOfList()))
                for (String value : search.getValueLikeAnyOfList())
                    predicatesAny.add(cb.like(fieldExpression, value, escapeChar));
            List<Predicate> predicatesAll = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search.getValueLikeAllOfList()))
                for (String value : search.getValueLikeAllOfList())
                    predicatesAll.add(cb.like(fieldExpression, value, escapeChar));

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
                    excludePredicatesAny.add(cb.notLike(fieldExpression, value, escapeChar));
            List<Predicate> excludePredicatesAll = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search.getValueLikeNoAllOfList()))
                for (String value : search.getValueLikeNoAllOfList())
                    excludePredicatesAll.add(cb.notLike(fieldExpression, value, escapeChar));

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

    //TODO    Need a load test to compare subquery execution speed with join
    public static Specification<TwinEntity> checkFieldList(final TwinFieldSearchList search) {
        return (root, query, cb) -> {
            if(search.isEmptySearch()) return cb.conjunction();
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
                includeAll = getPredicate(cb, allOfPredicates, false);
                cb.and(allOfPredicates.toArray(new Predicate[0]));
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

    public static Specification<TwinEntity> checkFieldBoolean(final TwinFieldSearchBoolean search) {
        return (root, query, cb) -> {
            if (search.isEmptySearch()) {
                return cb.conjunction();
            }

            return getPredicateForBoolean(root, cb , search);
        };
    }

    public static Specification<TwinEntity> checkFieldBooleanWithPhantoms(final TwinFieldSearchBoolean search, Boolean defaultValue) {
        return (root, query, cb) -> {
            if (search.isEmptySearch()) {
                return cb.conjunction();
            } else if (search.getValue().equals(defaultValue)) {
                //  left join twin_field_boolean
                Join<TwinEntity, TwinFieldBooleanEntity> tfbJoin = root.join(TwinEntity.Fields.fieldsBoolean, JoinType.LEFT);
                tfbJoin.on(
                        cb.equal(tfbJoin.get(TwinFieldBooleanEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId())
                );

                Predicate missingBooleanRecord = cb.isNull(tfbJoin.get(TwinFieldBooleanEntity.Fields.twinId));
                Predicate valueEqualsSearch = cb.equal(tfbJoin.get(TwinFieldBooleanEntity.Fields.value), search.getValue());

                //  equivalent to inner join twin_class_field (we don't have list of TwinClassFieldEntity fields to join them)
                Subquery<Long> tcfExists = query.subquery(Long.class);
                Root<TwinClassFieldEntity> tcfRoot = tcfExists.from(TwinClassFieldEntity.class);
                tcfExists.select(cb.literal(1L));
                tcfExists.where(
                        cb.equal(tcfRoot.get(TwinClassFieldEntity.Fields.id), search.getTwinClassFieldEntity().getId()),
                        cb.equal(tcfRoot.get(TwinClassFieldEntity.Fields.twinClassId), root.get(TwinEntity.Fields.twinClassId))
                );

                return cb.and(cb.exists(tcfExists), cb.or(missingBooleanRecord, valueEqualsSearch));
            } else {
                return getPredicateForBoolean(root, cb, search);
            }
        };
    }

    private static Predicate getPredicateForBoolean(Root<TwinEntity> root, CriteriaBuilder cb, final TwinFieldSearchBoolean search) {
        Join<TwinEntity, TwinFieldBooleanEntity> twinFieldBooleanJoin = root.join(TwinEntity.Fields.fieldsBoolean, JoinType.INNER);
        twinFieldBooleanJoin.on(cb.equal(twinFieldBooleanJoin.get(TwinFieldBooleanEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()));

        Expression<Boolean> booleanField = twinFieldBooleanJoin.get(TwinFieldBooleanEntity.Fields.value);

        return cb.equal(booleanField, search.getValue());
    }

    public static Specification<TwinEntity> checkFieldTimestamp(TwinFieldSearchDate search) {
        return (root, query, cb) -> {
            if (search.isEmptySearch()) return cb.conjunction();

            Join<TwinEntity, TwinFieldTimestampEntity> join = root.join(TwinEntity.Fields.fieldsTimestamp, JoinType.INNER);
            join.on(cb.equal(join.get(TwinFieldTimestampEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()));

            Expression<Timestamp> timestampField = join.get(TwinFieldTimestampEntity.Fields.value);
            java.util.List<Predicate> predicates = new java.util.ArrayList<>();

            if (search.getEquals() != null)
                predicates.add(cb.equal(timestampField, convertToTimestamp(search.getEquals())));
            if (search.getLessThen() != null)
                predicates.add(cb.lessThan(timestampField, convertToTimestamp(search.getLessThen())));
            if (search.getMoreThen() != null)
                predicates.add(cb.greaterThan(timestampField, convertToTimestamp(search.getMoreThen())));

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Timestamp convertToTimestamp(java.time.LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime);
    }

    public static Specification<TwinEntity> checkFieldTwinClassList(final TwinFieldSearchTwinClassList search) {
        return (root, query, cb) -> {
            if(search.isEmptySearch()) {
                return cb.conjunction();
            }

            Join<TwinEntity, TwinFieldTwinClassEntity> twinFieldTwinClassJoin = root.join(TwinEntity.Fields.fieldsTwinClassList, JoinType.INNER);
            twinFieldTwinClassJoin.on(cb.equal(twinFieldTwinClassJoin.get(TwinFieldTwinClassEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()));

            Predicate includeAny;
            if (CollectionUtils.isNotEmpty(search.getIdIncludeAnySet())) {
                includeAny = twinFieldTwinClassJoin.get(TwinFieldTwinClassEntity.Fields.twinClassId).in(search.getIdIncludeAnySet());
            } else {
                includeAny = cb.conjunction();
            }

            Predicate includeAll;
            if (CollectionUtils.isNotEmpty(search.getIdIncludeAllSet())) {
                List<Predicate> includeAllPredicates = new ArrayList<>();
                for (UUID id : search.getIdIncludeAllSet()) {
                    Subquery<Long> subquery = cb.createQuery().subquery(Long.class);
                    Root<TwinFieldTwinClassEntity> subRoot = subquery.from(TwinFieldTwinClassEntity.class);
                    subquery.select(cb.literal(1L));
                    subquery.where(
                            cb.equal(subRoot.get(TwinFieldTwinClassEntity.Fields.twinId), root.get(TwinEntity.Fields.id)),
                            cb.equal(subRoot.get(TwinFieldTwinClassEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()),
                            cb.equal(subRoot.get(TwinFieldTwinClassEntity.Fields.twinClassId), id)
                    );
                    includeAllPredicates.add(cb.exists(subquery));
                }
                includeAll = cb.and(includeAllPredicates.toArray(new Predicate[0]));
            } else {
                includeAll = cb.conjunction();
            }

            Predicate excludeAny;
            if (CollectionUtils.isNotEmpty(search.getIdExcludeAnySet())) {
                Subquery<UUID> excludeAnySubquery = cb.createQuery().subquery(UUID.class);
                Root<TwinFieldTwinClassEntity> excludeAnyRoot = excludeAnySubquery.from(TwinFieldTwinClassEntity.class);
                excludeAnySubquery.select(excludeAnyRoot.get(TwinFieldTwinClassEntity.Fields.twinId));
                excludeAnySubquery.where(
                        cb.equal(excludeAnyRoot.get(TwinFieldTwinClassEntity.Fields.twinId), root.get(TwinEntity.Fields.id)),
                        cb.equal(excludeAnyRoot.get(TwinFieldTwinClassEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()),
                        excludeAnyRoot.get(TwinFieldTwinClassEntity.Fields.twinClassId).in(search.getIdExcludeAnySet())
                );

                excludeAny = cb.not(cb.exists(excludeAnySubquery));
            } else {
                excludeAny = cb.conjunction();
            }

            Predicate excludeAll;
            if (CollectionUtils.isNotEmpty(search.getIdExcludeAllSet())) {
                List<Predicate> excludeAllConditions = new ArrayList<>();
                for (UUID id : search.getIdExcludeAllSet()) {
                    Subquery<Long> excludeAllSubquery = cb.createQuery().subquery(Long.class);
                    Root<TwinFieldTwinClassEntity> excludeAllRoot = excludeAllSubquery.from(TwinFieldTwinClassEntity.class);
                    excludeAllSubquery.select(cb.literal(1L));
                    excludeAllSubquery.where(
                            cb.equal(excludeAllRoot.get(TwinFieldTwinClassEntity.Fields.twinId), root.get(TwinEntity.Fields.id)),
                            cb.equal(excludeAllRoot.get(TwinFieldTwinClassEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()),
                            cb.equal(excludeAllRoot.get(TwinFieldTwinClassEntity.Fields.twinClassId), id)
                    );
                    excludeAllConditions.add(cb.exists(excludeAllSubquery));
                }

                excludeAll = cb.not(cb.and(excludeAllConditions.toArray(new Predicate[0])));
            } else {
                excludeAll = cb.conjunction();
            }

            Predicate include = cb.and(includeAny, includeAll);
            Predicate exclude = cb.and(excludeAny, excludeAll);

            return cb.and(include, exclude);
        };
    }

    public static Specification<TwinEntity> checkFieldUser(final TwinFieldSearchUser search) {
        return (root, query, cb) -> {
            if(search.isEmptySearch()) return cb.conjunction();
            Join<TwinEntity, TwinFieldUserEntity> twinFieldUserJoin = root.join(TwinEntity.Fields.fieldsUser, JoinType.INNER);
            twinFieldUserJoin.on(cb.equal(twinFieldUserJoin.get(TwinFieldUserEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()));

            Predicate include;
            if (CollectionUtils.isNotEmpty(search.getIdList())) {
                include = twinFieldUserJoin.get(TwinFieldUserEntity.Fields.userId).in(search.getIdList());
            } else {
                include = cb.conjunction();
            }

            Predicate exclude;
            if (CollectionUtils.isNotEmpty(search.getIdExcludeList())) {
                exclude = cb.not(twinFieldUserJoin.get(TwinFieldUserEntity.Fields.userId).in(search.getIdExcludeList()));
            } else {
                exclude = cb.conjunction();
            }

            return cb.and(include, exclude);
        };
    }

    public static Specification<TwinEntity> checkSpaceRoleUser(final TwinFieldSearchSpaceRoleUser search) {
        return (root, query, cb) -> {
            if(search.isEmptySearch()) return cb.conjunction();

            Join<TwinEntity, SpaceRoleUserEntity> spaceRoleUserJoin = root.join(TwinEntity.Fields.spaceRoleUsers, JoinType.INNER);

            Predicate roleInclude = cb.conjunction();
            if (CollectionUtils.isNotEmpty(search.getRoleIdList())) {
                roleInclude = spaceRoleUserJoin.get(SpaceRoleUserEntity.Fields.spaceRoleId).in(search.getRoleIdList());
            }

            Predicate roleExclude = cb.conjunction();
            if (CollectionUtils.isNotEmpty(search.getRoleIdExcludeList())) {
                roleExclude = cb.not(spaceRoleUserJoin.get(SpaceRoleUserEntity.Fields.spaceRoleId).in(search.getRoleIdExcludeList()));
            }

            Predicate userInclude = cb.conjunction();
            if (CollectionUtils.isNotEmpty(search.getUserIdList())) {
                userInclude = spaceRoleUserJoin.get(SpaceRoleUserEntity.Fields.userId).in(search.getUserIdList());
            }

            Predicate userExclude = cb.conjunction();
            if (CollectionUtils.isNotEmpty(search.getUserIdExcludeList())) {
                userExclude = cb.not(spaceRoleUserJoin.get(SpaceRoleUserEntity.Fields.userId).in(search.getUserIdExcludeList()));
            }

            return cb.and(roleInclude, roleExclude, userInclude, userExclude);
        };
    }
}
