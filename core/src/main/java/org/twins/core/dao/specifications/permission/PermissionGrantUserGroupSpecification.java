package org.twins.core.dao.specifications.permission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.UUID;

@Slf4j
public class PermissionGrantUserGroupSpecification extends CommonSpecification<PermissionGrantUserGroupEntity> {

    public static Specification<PermissionGrantUserGroupEntity> checkDomainId(UUID domainId) {
        Specification<PermissionGrantUserGroupEntity>  specification = Specification.allOf(
                checkFieldUuid(domainId, PermissionGrantUserGroupEntity.Fields.permissionSchema, PermissionSchemaEntity.Fields.domainId),
                (root, query, cb) -> createPredicateWithJoins(root, cb, domainId, (property, criteriaBuilder, filedValue) -> criteriaBuilder.or(criteriaBuilder.isNull(property), criteriaBuilder.equal(property, filedValue)), PermissionGrantUserGroupEntity.Fields.permission, PermissionEntity.Fields.permissionGroup, PermissionGroupEntity.Fields.domainId)
        );
        return specification;
    }

}
