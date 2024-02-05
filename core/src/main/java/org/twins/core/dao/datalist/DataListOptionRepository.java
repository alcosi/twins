package org.twins.core.dao.datalist;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DataListOptionRepository extends CrudRepository<DataListOptionEntity, UUID>, JpaSpecificationExecutor<DataListOptionEntity> {
    @Cacheable(value = "DataListOptionRepository.findByDataListId", key = "{#dataListId}")
    List<DataListOptionEntity> findByDataListId(UUID dataListId);

    @Cacheable(value = "DataListOptionRepository.findByDataListIdAndBusinessAccountId", key = "{#dataListId}")
    List<DataListOptionEntity> findByDataListIdAndBusinessAccountId(UUID dataListId, UUID businessAccountId);

    int countByDataListId(UUID dataListId);

    List<DataListOptionEntity> findByIdIn(Collection<UUID> dataListOptionId);
    List<DataListOptionEntity> findByIdInAndBusinessAccountId(Collection<UUID> dataListOptionId, UUID businessAccountId);

    Optional<DataListOptionEntity> findByIdAndBusinessAccountId(UUID dataListOptionId, UUID businessAccountId);

    @Query(value = "from DataListOptionEntity option " +
            "where option.dataListId = :dataListId " +
            "and option.id not in (select cast(field.value as uuid) from TwinFieldEntity field where field.twinClassFieldId = :twinClassFieldId )")
    List<DataListOptionEntity> findByDataListIdAndNotUsedInDomain(@Param("dataListId") UUID dataListId, @Param("twinClassFieldId") UUID twinClassFieldId);

    @Query(value = "from DataListOptionEntity option " +
            "where option.dataListId = :dataListId " +
            "and option.id not in (select cast(field.value as uuid) from TwinFieldEntity field where field.twinClassFieldId = :twinClassFieldId and field.twin.ownerBusinessAccountId = :businessAccountId )")
    List<DataListOptionEntity> findByDataListIdAndNotUsedInBusinessAccount(@Param("dataListId") UUID dataListId, @Param("twinClassFieldId") UUID twinClassFieldId, @Param("businessAccountId") UUID businessAccountId);

    @Query(value = "from DataListOptionEntity option " +
            "where option.dataListId = :dataListId " +
            "and option.id not in (select cast(field.value as uuid) from TwinFieldEntity field where field.twinClassFieldId = :twinClassFieldId and field.twin.headTwinId = :headTwinId )")
    List<DataListOptionEntity> findByDataListIdAndNotUsedInHead(@Param("dataListId") UUID dataListId, @Param("twinClassFieldId") UUID twinClassFieldId, @Param("headTwinId") UUID headTwinId);

}
