package org.twins.core.dao.specifications.permission;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.UUID;

@Slf4j
public class PermissionGrantUserGroupSpecification extends CommonSpecification<PermissionGrantUserGroupEntity> {

    public static Specification<PermissionGrantUserGroupEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            Join<PermissionGrantUserGroupEntity, PermissionSchemaEntity> permissionSchemaEntityJoin = root.join(PermissionGrantUserGroupEntity.Fields.permissionSchema, JoinType.INNER);
            Predicate permissionSchemaPredicate = cb.equal(permissionSchemaEntityJoin.get(PermissionSchemaEntity.Fields.domainId), domainId);

            Join<PermissionGrantUserGroupEntity, PermissionEntity> permissionJoin = root.join(PermissionGrantUserGroupEntity.Fields.permission, JoinType.INNER);
            Join<PermissionEntity, PermissionGroupEntity> permissionGroupJoin = permissionJoin.join(PermissionEntity.Fields.permissionGroup, JoinType.INNER);

            Predicate domainIdPredicate = cb.equal(permissionGroupJoin.get(PermissionGroupEntity.Fields.domainId), domainId);
            Predicate domainNullPredicate = cb.isNull(permissionGroupJoin.get(PermissionGroupEntity.Fields.domainId));
            Predicate domainCondition = cb.or(domainIdPredicate, domainNullPredicate);

            return cb.and(permissionSchemaPredicate, domainCondition);
        };
    }

}
