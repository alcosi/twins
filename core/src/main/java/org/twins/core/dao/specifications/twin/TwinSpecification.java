package org.twins.core.dao.specifications.twin;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.specifications.AbstractTwinEntityBasicSearchSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.domain.search.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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


    public static Specification<TwinEntity> checkFieldNumeric(final TwinFieldSearchNumeric search) throws ServiceException {
        return (root, query, cb) -> {
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

    public static Specification<TwinEntity> checkFieldDate(final TwinFieldSearchDate search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<TwinEntity, TwinFieldSimpleEntity> twinFieldSimpleJoin = root.join(TwinEntity.Fields.fieldsSimple, JoinType.INNER);
            twinFieldSimpleJoin.on(cb.equal(twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.twinClassFieldId), search.getTwinClassFieldEntity().getId()));


            Expression<String> stringValue = twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.value);
            Expression<LocalDateTime> dateTimeValue = cb.function("text2timestamp", LocalDateTime.class, twinFieldSimpleJoin.get(TwinFieldSimpleEntity.Fields.value));

            Predicate lessAndMore = null;
            Predicate equals = null;
            if (search.getLessThen() != null || search.getMoreThen() != null || search.getEquals() != null) {
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

            if (search.isEmpty())
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

    //TODO    Need a load test to compare subquery execution speed with join
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
}
