package org.twins.core.dao.permission;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionGrantGlobalRepository extends CrudRepository<PermissionGrantGlobalEntity, UUID>, JpaSpecificationExecutor<PermissionGrantGlobalEntity> {
    @Query(value = "select distinct pgg.permissionId from PermissionGrantGlobalEntity pgg where pgg.userGroupId in :userGroupIdList " +
            "and (pgg.permission.permissionGroup.domainId is null) and pgg.userGroup.userGroupType.sluggerFeaturerId = 2005")
    List<UUID> findPermissionIdByUserGroupIdIn(Collection<UUID> userGroupIdList);

}
