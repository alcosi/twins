package org.twins.core.dao.permission;

import org.hibernate.query.TypedParameterValue;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends CrudRepository<PermissionEntity, UUID>, JpaSpecificationExecutor<PermissionEntity> {
    @Query(value = "select function('permission_check_mater', :permissionSchemaId, :permissionId, :permissionSpaceId, :userId, :userGroupFootprintId, :twinClassId, :isAssignee, :isCreator)")
    boolean hasPermission(
            @Param("permissionSchemaId") UUID permissionSchemaId,
            @Param("permissionId") UUID permissionId,
            @Param("permissionSpaceId") TypedParameterValue<UUID> permissionSpaceId,
            @Param("userId") UUID userId,
            @Param("userGroupFootprintId") UUID userGroupFootprintId,
            @Param("twinClassId") UUID twinClassId,
            @Param("isAssignee") boolean isAssignee,
            @Param("isCreator") boolean isCreator);

    boolean existsByIdAndPermissionGroup_DomainId(UUID permissionId, UUID domainId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PermissionEntity p " +
           "WHERE p.id = :permissionId AND (p.permissionGroup.domainId = :domainId OR p.permissionGroup.domainId IS NULL)")
    boolean existsByIdAndPermissionGroup_DomainIdOrDomainIdIsNull(@Param("permissionId") UUID permissionId, @Param("domainId") UUID domainId);

    List<PermissionEntity> findByIdIn(Collection<UUID> ids);

    Optional<PermissionEntity> findByPermissionGroup_DomainIdAndKey(UUID uuid, String key);
}
