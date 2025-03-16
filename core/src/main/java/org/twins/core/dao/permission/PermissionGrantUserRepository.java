package org.twins.core.dao.permission;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionGrantUserRepository extends CrudRepository<PermissionGrantUserEntity, UUID>, JpaSpecificationExecutor<PermissionGrantUserEntity> {
    List<PermissionGrantUserEntity> findByPermissionSchemaIdAndUserId(UUID permissionSchemaId, UUID userId);

    @Query(value = "select distinct psu.permissionId from PermissionGrantUserEntity psu where psu.userId = :userId " +
            "and psu.permissionSchemaId = :permissionSchemaId " +
            "and (psu.permission.permissionGroup.domainId = psu.permissionSchema.domainId or psu.permission.permissionGroup.domainId is null)")
    List<UUID> findPermissionIdByPermissionSchemaIdAndUserId(UUID permissionSchemaId, UUID userId);

    List<PermissionGrantUserEntity> findByPermissionSchemaIdAndUserIdAndPermission_PermissionGroup_DomainId(UUID permissionSchemaId, UUID userId, UUID domainId);

    boolean existsByPermissionSchemaIdAndPermissionIdAndUserId(UUID permissionSchemaId, UUID permissionId, UUID userId);

    @Query(value = "SELECT * FROM permissions_load_for_user(:permissionSchemaId, :userId, CAST(:userGroupIds AS UUID[]))", nativeQuery = true)
    List<UUID> findAllPermissionsForUser(UUID permissionSchemaId, UUID userId, String userGroupIds);
}
