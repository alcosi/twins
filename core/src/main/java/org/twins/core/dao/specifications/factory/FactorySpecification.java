package org.twins.core.dao.specifications.factory;

import jakarta.persistence.criteria.Predicate;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class FactorySpecification extends CommonSpecification<TwinFactoryEntity> {

    public static Specification<TwinFactoryEntity> checkFieldLikeIn(String field, Collection<String> search, boolean not, boolean or) {
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

    public static Specification<TwinFactoryEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            if (domainId == null)
                return cb.disjunction();
            return cb.equal(root.get(TwinFactoryEntity.Fields.domainId), domainId);
        };
    }
}
