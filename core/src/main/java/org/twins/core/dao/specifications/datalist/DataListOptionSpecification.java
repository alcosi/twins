package org.twins.core.dao.specifications.datalist;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
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

    public static Specification<DataListOptionEntity> empty() {
        return (root, query, cb) -> {
            return cb.conjunction();
        };
    }

    public static Specification<DataListOptionEntity> checkFieldLikeIn(String field, Collection<String> search, boolean not, boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            List<Predicate> predicates = new ArrayList<>();
            for (String value : search) {
                Predicate predicate = cb.like(cb.lower(root.get(field)), value.toLowerCase());
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

            Join<DataListOptionEntity, DataListEntity> joinDataListOption = root.join(DataListOptionEntity.Fields.dataList, JoinType.INNER);

            List<Predicate> predicates = new ArrayList<>();
            for (String value : search) {
                Predicate predicate = cb.like(cb.lower(joinDataListOption.get(DataListEntity.Fields.key)), value.toLowerCase());
                if (not) predicate = cb.not(predicate);
                predicates.add(predicate);
            }
            return getPredicate(cb, predicates, or);
        };
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

    public static Specification<DataListOptionEntity> checkDataListSubsetOption(Collection<String> search, boolean not, boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            query.distinct(true);

            Join<DataListOptionEntity, DataListSubsetOptionEntity> subsetOptionJoin = root.join(DataListOptionEntity.Fields.subsetOptions, JoinType.INNER);
            Join<DataListSubsetOptionEntity, DataListSubsetEntity> subsetJoin = subsetOptionJoin.join(DataListSubsetOptionEntity.Fields.dataListSubset, JoinType.INNER);
            List<Predicate> predicates = new ArrayList<>();
            for (String value : search) {
                Predicate predicate = cb.like(cb.lower(subsetJoin.get(DataListSubsetEntity.Fields.key)), value.toLowerCase());
                if (not) predicate = cb.not(predicate);
                predicates.add(predicate);
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<DataListOptionEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            if (domainId == null)
                return cb.disjunction();
            Join<DataListOptionEntity, DataListEntity> joinDataListOption = root.join(DataListOptionEntity.Fields.dataList, JoinType.INNER);
            return cb.equal(joinDataListOption.get(DataListEntity.Fields.domainId), domainId);
        };
    }
}
