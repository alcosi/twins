package org.twins.core.dao.twin;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.enums.status.StatusType;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinStatusRepository extends CrudRepository<TwinStatusEntity, UUID>, JpaSpecificationExecutor<TwinStatusEntity> {
    String CACHE_TWIN_STATUS_TYPE = "TwinStatusRepository.existsByIdAndType";

    List<TwinStatusEntity> findByTwinClassId(UUID twinClassId);

    List<TwinStatusEntity> findByTwinClassIdIn(Set<UUID> twinClassIdList);

    @Cacheable(value = CACHE_TWIN_STATUS_TYPE, key = "#statusId + '' + #type")
    boolean existsByIdAndType(UUID statusId, StatusType type);
}
