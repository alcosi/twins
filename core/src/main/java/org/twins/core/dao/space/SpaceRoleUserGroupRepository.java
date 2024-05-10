package org.twins.core.dao.space;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpaceRoleUserGroupRepository extends CrudRepository<SpaceRoleUserGroupEntity, UUID>, JpaSpecificationExecutor<SpaceRoleUserGroupEntity> {

    List<SpaceRoleUserGroupEntity> findAllByTwinIdAndUserGroupIdIn(UUID twinId, Collection<UUID> groupIds);

}

