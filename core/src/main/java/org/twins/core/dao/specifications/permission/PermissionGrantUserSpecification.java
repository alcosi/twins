package org.twins.core.dao.specifications.permission;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.permission.PermissionGrantUserEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.UUID;

@Slf4j
public class PermissionGrantUserSpecification extends CommonSpecification<PermissionGrantUserEntity> {

    public static Specification<PermissionGrantUserEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            Join<PermissionGrantUserEntity, PermissionSchemaEntity> permissionGrantUserEntityJoin = root.join(PermissionGrantUserEntity.Fields.permissionSchema, JoinType.INNER);
            return cb.equal(permissionGrantUserEntityJoin.get(PermissionSchemaEntity.Fields.domainId), domainId);
        };
    }
}
