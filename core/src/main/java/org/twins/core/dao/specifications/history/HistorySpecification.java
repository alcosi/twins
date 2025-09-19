package org.twins.core.dao.specifications.history;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.DataTimeRange;

import java.util.Collection;
import java.util.UUID;

@Slf4j
public class HistorySpecification extends CommonSpecification<HistoryEntity> {

    public static Specification<HistoryEntity> checkByTwinIdIncludeFirstLevelChildren(final Collection<UUID> twinIds, Boolean include, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(twinIds)) return cb.conjunction();

            Predicate historyPredicate = root.get(HistoryEntity.Fields.twinId).in(twinIds);
            Predicate childTwinIdPredicate = cb.disjunction();

            // we check for the presence of child twins ONLY for twins INCLUDE in the list
            // if NOT is ture (don't supporting)
            if (include && !not) {
                Join<HistoryEntity, TwinEntity> twinJoin = root.join(HistoryEntity.Fields.twin, JoinType.LEFT);
                childTwinIdPredicate = twinJoin.get(TwinEntity.Fields.headTwinId).in(twinIds);
            }

            if (not) historyPredicate = cb.not(historyPredicate);
            return cb.or(historyPredicate, childTwinIdPredicate);
        };
    }

    public static Specification<HistoryEntity> checkType(final Collection<HistoryType> types, final boolean exclude) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(types)) return cb.conjunction();

            Predicate predicate = root.get(HistoryEntity.Fields.historyType).in(types);
            if (exclude) predicate = cb.not(predicate);
            return predicate;
        };
    }

    public static Specification<HistoryEntity> createdAtBetween(final DataTimeRange createdAt) {
        return (root, query, cb) -> {
            if (createdAt == null || (createdAt.getFrom() == null && createdAt.getTo() == null)) return cb.conjunction();

            Predicate predicate = cb.conjunction();
            if (createdAt.getFrom() != null)
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get(HistoryEntity.Fields.createdAt), createdAt.getFrom()));
            if (createdAt.getTo() != null)
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get(HistoryEntity.Fields.createdAt), createdAt.getTo()));
            return predicate;
        };
    }
}
