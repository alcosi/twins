package org.twins.core.dao.space;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpaceRoleUserGroupRepository extends CrudRepository<SpaceRoleUserGroupEntity, UUID>, JpaSpecificationExecutor<SpaceRoleUserGroupEntity> {
}
