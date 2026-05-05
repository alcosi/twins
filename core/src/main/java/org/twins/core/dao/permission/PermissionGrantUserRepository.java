package org.twins.core.dao.permission;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionGrantUserRepository extends CrudRepository<PermissionGrantUserEntity, UUID>, JpaSpecificationExecutor<PermissionGrantUserEntity> {
    List<PermissionGrantUserEntity> findByPermissionSchemaIdAndUserId(UUID permissionSchemaId, UUID userId);

    List<PermissionGrantUserEntity> findByPermissionSchemaIdAndUserIdAndPermission_PermissionGroup_DomainId(UUID permissionSchemaId, UUID userId, UUID domainId);

    boolean existsByPermissionSchemaIdAndPermissionIdAndUserId(UUID permissionSchemaId, UUID permissionId, UUID userId);

    @Query(value = "SELECT * FROM permissions_get(:permissionSchemaId, :userId, :userGroupsFootprint)", nativeQuery = true)
    @Cacheable(value = "userPermissionsCache", key = "{#permissionSchemaId, #userId, #userGroupsFootprint}")
    List<UUID> findAllPermissionsForUser(UUID permissionSchemaId, UUID userId, UUID userGroupsFootprint);
}
