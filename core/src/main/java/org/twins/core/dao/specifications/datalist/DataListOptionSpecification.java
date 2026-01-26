package org.twins.core.dao.specifications.datalist;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListSubsetEntity;
import org.twins.core.dao.datalist.DataListSubsetOptionEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class DataListOptionSpecification extends CommonSpecification<DataListOptionEntity> {
    public static Specification<DataListOptionEntity> checkStatusLikeIn(Collection<String> search, boolean not, boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            List<Predicate> predicates = new ArrayList<>();
            for (String value : search) {
                Predicate predicate = cb.like(cb.lower(root.get(DataListOptionEntity.Fields.status)), value.toLowerCase(), escapeChar);
                if (not) predicate = cb.not(predicate);
                predicates.add(predicate);
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<DataListOptionEntity> checkDataListKeyLikeIn(Collection<String> search, boolean not, boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            // include double join
            Join<DataListOptionEntity, ?> joinDataListOption = getOrCreateJoin(root);

            List<Predicate> predicates = new ArrayList<>();
            for (String value : search) {
                Predicate predicate = cb.like(cb.lower(joinDataListOption.get(DataListEntity.Fields.key)), value.toLowerCase(), escapeChar);
                if (not) predicate = cb.not(predicate);
                predicates.add(predicate);
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Join<DataListOptionEntity, ?> getOrCreateJoin(Root<DataListOptionEntity> root) {
        return root.getJoins().stream()
                .filter(j -> j.getAttribute().getName().equals(DataListOptionEntity.Fields.dataList))
                .findFirst()
                .orElseGet(() -> root.join(DataListEntity.Fields.key, JoinType.LEFT));
    }

    public static Specification<DataListOptionEntity> checkDataListSubset(Collection<UUID> search, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            query.distinct(true);

            Join<DataListOptionEntity, DataListSubsetOptionEntity> subsetOptionJoin = root.join(DataListOptionEntity.Fields.subsetOptions, JoinType.INNER);

            Predicate predicate = subsetOptionJoin.get(DataListSubsetOptionEntity.Fields.dataListSubsetId).in(search);
            if (not) predicate = cb.not(predicate);
            return predicate;
        };
    }

    public static Specification<DataListOptionEntity> checkDataListSubsetKey(Collection<String> search, boolean not, boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            query.distinct(true);

            Join<DataListOptionEntity, DataListSubsetOptionEntity> subsetOptionJoin = root.join(DataListOptionEntity.Fields.subsetOptions, JoinType.INNER);
            Join<DataListSubsetOptionEntity, DataListSubsetEntity> subsetJoin = subsetOptionJoin.join(DataListSubsetOptionEntity.Fields.dataListSubset, JoinType.INNER);
            List<Predicate> predicates = new ArrayList<>();
            for (String value : search) {
                Predicate predicate = cb.like(cb.lower(subsetJoin.get(DataListSubsetEntity.Fields.key)), value.toLowerCase(), escapeChar);
                if (not) predicate = cb.not(predicate);
                predicates.add(predicate);
            }
            return getPredicate(cb, predicates, or);
        };
    }

}
