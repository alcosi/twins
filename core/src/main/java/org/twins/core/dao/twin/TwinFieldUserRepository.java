package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinFieldUserRepository extends CrudRepository<TwinFieldUserEntity, UUID>, JpaSpecificationExecutor<TwinFieldUserEntity> {
    List<TwinFieldUserEntity> findByTwinFieldId(UUID twinFieldId);

    List<TwinFieldUserEntity> findByTwinId(UUID twinId);

    List<TwinFieldUserEntity> findByTwinFieldIdAndUserId(UUID twinFieldId, UUID dataListOptionId);

}
