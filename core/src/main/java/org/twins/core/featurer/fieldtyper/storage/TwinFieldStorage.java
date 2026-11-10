package org.twins.core.featurer.fieldtyper.storage;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.specifications.GroupExpressionProvider;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class TwinFieldStorage {
    public abstract void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException;

    public abstract boolean hasStrictValues(UUID twinClassFieldId);

    public abstract boolean isLoaded(TwinEntity twinEntity);

    public abstract void initEmpty(TwinEntity twinEntity);

    public abstract Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet);

    public abstract void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId);

    public abstract void deleteTwinFieldsForTwins(Map<UUID, Set<UUID>> deleteMap);

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || canBeMerged(obj);
    }

    /**
     * This method helps to merge storages from different twin class fields for load data.
     * For example, we have class with 10 fields. They all are simple, so we can load them all at once by on storage
     * @param o
     * @return
     */
    abstract boolean canBeMerged(Object o);

    protected boolean isSameClass(Object o) {
        return o != null && o.getClass() == this.getClass();
    }

    /**
     * GROUP BY expression for the twin count API.
     * {@code null} means this storage is not a count dimension.
     * A new storage does not compile until it decides: return a column group, or {@code null}.
     *
     * @param distinct count each twin once per group. Used by storages that can keep several
     *                 rows of one field on one twin (a multiple datalist). Other storages ignore it.
     */
    public abstract GroupExpressionProvider<TwinEntity> countGroup(UUID fieldId, boolean distinct);

    protected final GroupExpressionProvider<TwinEntity> countGroupByColumn(UUID fieldId, String collection, String attribute) {
        return countGroupByColumn(fieldId, collection, attribute, false);
    }

    protected final GroupExpressionProvider<TwinEntity> countGroupByColumn(UUID fieldId, String collection, String attribute, boolean distinct) {
        return new GroupExpressionProvider<>() {
            @Override
            public Expression<?> apply(Root<TwinEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return TwinSpecification.getOrCreateFieldJoin(root, cb, fieldId, collection, JoinType.LEFT).get(attribute);
            }

            @Override
            public boolean isDistinctGroup() {
                return distinct;
            }
        };
    }
}
