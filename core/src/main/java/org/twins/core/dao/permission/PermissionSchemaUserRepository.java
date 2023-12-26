package org.twins.core.dao.permission;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionSchemaUserRepository extends CrudRepository<PermissionSchemaUserEntity, UUID>, JpaSpecificationExecutor<PermissionSchemaUserEntity> {
    List<PermissionSchemaUserEntity> findByPermissionSchemaIdAndUserId(UUID permissionSchemaId, UUID userId);

    @Query(value = "select distinct psu.permissionId from PermissionSchemaUserEntity psu where psu.userId = :userId " +
            "and psu.permissionSchemaId = :permissionSchemaId " +
            "and (psu.permission.permissionGroup.domainId = psu.permissionSchema.domainId or psu.permission.permissionGroup.domainId is null)")
    List<UUID> findPermissionIdByPermissionSchemaIdAndUserId(UUID permissionSchemaId, UUID userId);

    List<PermissionSchemaUserEntity> findByPermissionSchemaIdAndUserIdAndPermission_PermissionGroup_DomainId(UUID permissionSchemaId, UUID userId, UUID domainId);
}
