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
    @Query(value = "select function('permission_check', :domainId, :businessAccountId, :permissionSpaceId, :permissionId, :userId, :userGroupId, :isAssignee, :isCreator, :twinClassId)")
    boolean permissionCheck(
            @Param("domainId") UUID domainId,
            @Param("businessAccountId") UUID businessAccountId,
            @Param("permissionSpaceId") UUID permissionSpaceId,
            @Param("permissionId") UUID permissionId,
            @Param("userId") UUID userId,
            @Param("userGroupId") TypedParameterValue<UUID[]> userGroupIds,
            @Param("twinClassId") UUID twinClassId,
            @Param("isAssignee") Boolean isAssignee,
            @Param("isCreator") Boolean isCreator);

    boolean existsByIdAndPermissionGroup_DomainId(UUID permissionId, UUID domainId);

    List<PermissionEntity> findByIdIn(Collection<UUID> ids);

    Optional<PermissionEntity> findByPermissionGroup_DomainIdAndKey(UUID uuid, String key);
}
