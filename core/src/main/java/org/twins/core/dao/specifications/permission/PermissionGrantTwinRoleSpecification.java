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


    public static Specification<PermissionGrantTwinRoleEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            Join<PermissionGrantTwinRoleEntity, PermissionSchemaEntity> permissionGrantUserEntityJoin = root.join(PermissionGrantTwinRoleEntity.Fields.permissionSchema, JoinType.INNER);
            return cb.equal(permissionGrantUserEntityJoin.get(PermissionSchemaEntity.Fields.domainId), domainId);
        };
    }

}
