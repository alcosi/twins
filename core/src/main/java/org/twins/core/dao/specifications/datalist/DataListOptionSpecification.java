package org.twins.core.dao.specifications.datalist;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class DataListOptionSpecification extends CommonSpecification<DataListOptionEntity> {

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

    public static Specification<DataListOptionEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            if (domainId == null)
                return cb.disjunction();
            Join<DataListOptionEntity, DataListEntity> joinDataListOption = root.join(DataListOptionEntity.Fields.dataList, JoinType.INNER);
            return cb.equal(joinDataListOption.get(DataListEntity.Fields.domainId), domainId);
        };
    }
}
