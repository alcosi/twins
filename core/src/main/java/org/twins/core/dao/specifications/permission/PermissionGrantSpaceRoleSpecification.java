package org.twins.core.dao.specifications.permission;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.permission.PermissionGrantSpaceRoleEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.UUID;

@Slf4j
public class PermissionGrantSpaceRoleSpecification extends CommonSpecification<PermissionGrantSpaceRoleEntity> {

    public static Specification<PermissionGrantSpaceRoleEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            Join<PermissionGrantSpaceRoleEntity, PermissionSchemaEntity> permissionSchemaJoin = root.join(PermissionGrantSpaceRoleEntity.Fields.permissionSchema, JoinType.INNER);
            return cb.equal(permissionSchemaJoin.get(PermissionSchemaEntity.Fields.domainId), domainId);
        };
    }

}
