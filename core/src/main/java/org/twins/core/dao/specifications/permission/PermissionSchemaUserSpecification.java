package org.twins.core.dao.specifications.permission;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaUserEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.UUID;

@Slf4j
public class PermissionSchemaUserSpecification extends CommonSpecification<PermissionSchemaUserEntity> {

    public static Specification<PermissionSchemaUserEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            Join<PermissionSchemaUserEntity, PermissionSchemaEntity> permissionSchemaUserEntityJoin = root.join(PermissionSchemaUserEntity.Fields.permissionSchema, JoinType.INNER);
            return cb.equal(permissionSchemaUserEntityJoin.get(PermissionSchemaEntity.Fields.domainId), domainId);
        };
    }
}
