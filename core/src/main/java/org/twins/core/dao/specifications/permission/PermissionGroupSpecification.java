package org.twins.core.dao.specifications.permission;

import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.UUID;


public class PermissionGroupSpecification extends CommonSpecification<PermissionEntity> {


    public static Specification<PermissionGroupEntity> checkDomainId(UUID domainId, boolean showSystemGroup) {
        if (showSystemGroup) {
            return (root, query, cb) -> createPredicateWithJoins(root, cb, domainId, (property, criteriaBuilder, filedValue) -> criteriaBuilder.or(criteriaBuilder.isNull(property), criteriaBuilder.equal(property, filedValue)), PermissionGroupEntity.Fields.domainId);
        } else {
            return checkFieldUuid(domainId, PermissionGroupEntity.Fields.domainId);

        }
    }
}
