package org.twins.core.dao.permission;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PermissionSchemaUserGroupRepository extends CrudRepository<PermissionSchemaUserGroupEntity, UUID>, JpaSpecificationExecutor<PermissionSchemaUserGroupEntity> {
    List<PermissionSchemaUserGroupEntity> findByPermissionSchemaIdAndUserGroupIdIn(UUID permissionSchemaId, List<UUID> userGroupIdList);

    List<PermissionSchemaUserGroupEntity> findByPermissionSchemaIdAndPermissionIdAndUserGroupIdIn(UUID permissionSchemaId, UUID permissionIdm, Collection<UUID> userGroupIdList);

    @Query(value = "select distinct psu.permissionId from PermissionSchemaUserGroupEntity psu where psu.userGroupId in :userGroupIdList " +
            "and psu.permissionSchemaId = :permissionSchemaId " +
            "and (psu.permission.permissionGroup.domainId = psu.permissionSchema.domainId or psu.permission.permissionGroup.domainId is null)")
    List<UUID> findPermissionIdByPermissionSchemaIdAndUserGroupIdIn(UUID permissionSchemaId, Set<UUID> userGroupIdList);
}
