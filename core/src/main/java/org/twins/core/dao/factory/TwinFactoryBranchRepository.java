package org.twins.core.dao.factory;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinFactoryBranchRepository extends CrudRepository<TwinFactoryBranchEntity, UUID>, JpaSpecificationExecutor<TwinFactoryBranchEntity> {
    List<TwinFactoryBranchEntity> findByTwinFactoryIdAndActiveTrue(UUID twinFactoryId);

    @Query("SELECT b.nextTwinFactoryId, COUNT(b) FROM TwinFactoryBranchEntity b WHERE b.nextTwinFactoryId IN :ids GROUP BY b.nextTwinFactoryId")
    List<Object[]> countByNextTwinFactoryIds(Collection<UUID> ids);

    @Query("SELECT t.twinFactoryId, COUNT(t) FROM TwinFactoryBranchEntity t WHERE t.twinFactoryId IN :ids GROUP BY t.twinFactoryId")
    List<Object[]> countByTwinFactoryIds(Collection<UUID> ids);
}
