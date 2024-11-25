package org.twins.core.dao.specifications.permission;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.permission.*;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

@Slf4j
public class PermissionGrantTwinRoleSpecification extends CommonSpecification<PermissionGrantTwinRoleEntity> {

    public static Specification<PermissionGrantTwinRoleEntity> checkFieldLikeIn(final String field, final Collection<String> search, final boolean not, final boolean or) {
        return (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search)) {
                for (String name : search) {
                    Predicate predicate = cb.like(cb.lower(root.get(field)),  name.toLowerCase() );
                    if (not) predicate = cb.not(predicate);
                    predicates.add(predicate);
                }
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<PermissionGrantTwinRoleEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            Join<PermissionGrantTwinRoleEntity, PermissionSchemaEntity> permissionGrantUserEntityJoin = root.join(PermissionGrantTwinRoleEntity.Fields.permissionSchema, JoinType.INNER);
            return cb.equal(permissionGrantUserEntityJoin.get(PermissionSchemaEntity.Fields.domainId), domainId);
        };
    }

}
