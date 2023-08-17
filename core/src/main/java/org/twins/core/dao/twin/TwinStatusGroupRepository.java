package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.space.SpaceRoleUserEntity;

import java.util.UUID;

@Repository
public interface TwinStatusGroupRepository extends CrudRepository<TwinStatusGroupEntity, UUID>, JpaSpecificationExecutor<TwinStatusGroupEntity> {
}
