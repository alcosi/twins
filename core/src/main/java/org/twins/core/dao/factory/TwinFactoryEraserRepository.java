package org.twins.core.dao.factory;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinFactoryEraserRepository extends CrudRepository<TwinFactoryEraserEntity, UUID>, JpaSpecificationExecutor<TwinFactoryEraserEntity> {
    List<TwinFactoryEraserEntity> findByTwinFactoryIdAndActiveTrue(UUID twinFactoryId);

    @Query("SELECT t.twinFactoryId, COUNT(t) FROM TwinFactoryEraserEntity t WHERE t.twinFactoryId IN :ids GROUP BY t.twinFactoryId")
    List<Object[]> countByTwinFactoryIds(Collection<UUID> ids);

    @Query("SELECT t.twinFactoryConditionSetId, COUNT(t) FROM TwinFactoryEraserEntity t WHERE t.twinFactoryConditionSetId IN :ids GROUP BY t.twinFactoryConditionSetId")
    List<Object[]> countByConditionSetIds(Collection<UUID> ids);
}
