package org.twins.core.dao.specifications.permission;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.UUID;


public class PermissionSpecification extends CommonSpecification<PermissionEntity> {

    public static Specification<PermissionEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> createPredicateWithJoins(root, cb, domainId, (property, criteriaBuilder, filedValue) -> criteriaBuilder.or(criteriaBuilder.isNull(property), criteriaBuilder.equal(property, filedValue)), JoinType.INNER, PermissionEntity.Fields.permissionGroup, PermissionGroupEntity.Fields.domainId);
    }
}
