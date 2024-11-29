package org.twins.core.dao.specifications.permission;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.UUID;

@Slf4j
public class PermissionGrantAssigneePropagationSpecification extends CommonSpecification<PermissionGrantAssigneePropagationEntity> {

    public static Specification<PermissionGrantAssigneePropagationEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            Join<PermissionGrantAssigneePropagationEntity, PermissionSchemaEntity> permissionSchemaJoin = root.join(PermissionGrantAssigneePropagationEntity.Fields.permissionSchema, JoinType.INNER);
            return cb.equal(permissionSchemaJoin.get(PermissionSchemaEntity.Fields.domainId), domainId);
        };
    }

}
