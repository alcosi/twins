package org.twins.core.dao.specifications.permission;

import jakarta.persistence.criteria.Predicate;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class PermissionGroupSpecification extends CommonSpecification<PermissionEntity> {

    public static Specification<PermissionGroupEntity> checkFieldLikeIn(final String field, final Collection<String> search, final boolean not, final boolean or) {
        return (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search)) {
                for (String name : search) {
                    Predicate predicate = cb.like(cb.lower(root.get(field)), "%" + name.toLowerCase() + "%");
                    if (not) predicate = cb.not(predicate);
                    predicates.add(predicate);
                }
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<PermissionGroupEntity> checkDomainId(UUID domainId, boolean showSystemGroup) {
        return (root, query, cb) -> {
            if (domainId == null)
                return cb.conjunction();
            if (showSystemGroup)
                return cb.or(
                        cb.isNull(root.get(PermissionGroupEntity.Fields.domainId)),
                        cb.equal(root.get(PermissionGroupEntity.Fields.domainId), domainId)
                );
            return cb.equal(root.get(PermissionGroupEntity.Fields.domainId), domainId);
        };
    }
}
