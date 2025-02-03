package org.twins.core.dao.resource;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StorageRepository extends CrudRepository<StorageEntity, UUID>, JpaSpecificationExecutor<ResourceEntity> {

    <T> T findById(UUID id, Class<T> type);

}
