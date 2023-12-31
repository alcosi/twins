package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinFieldDataListRepository extends CrudRepository<TwinFieldDataListEntity, UUID>, JpaSpecificationExecutor<TwinFieldDataListEntity> {
    List<TwinFieldDataListEntity> findByTwinFieldId(UUID twinFieldId);

    List<TwinFieldDataListEntity> findByTwinField_TwinId(UUID twinId);

    List<TwinFieldDataListEntity> findByTwinFieldIdAndDataListOptionId(UUID twinFieldId, UUID dataListOptionId);

}
