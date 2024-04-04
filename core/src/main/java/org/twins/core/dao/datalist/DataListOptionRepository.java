package org.twins.core.dao.datalist;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface DataListOptionRepository extends CrudRepository<DataListOptionEntity, UUID>, JpaSpecificationExecutor<DataListOptionEntity> {
    @Cacheable(value = "DataListOptionRepository.findByDataListId", key = "{#dataListId}")
    @Query(value = "from DataListOptionEntity option where option.dataListId = :dataListId and option.businessAccountId is null order by option.order")
    List<DataListOptionEntity> findByDataListId(@Param("dataListId") UUID dataListId);

    @Cacheable(value = "DataListOptionRepository.findByDataListIdAndBusinessAccountId", key = "{#dataListId, #businessAccountId}")
    @Query(value = "from DataListOptionEntity option where option.dataListId = :dataListId and (option.businessAccountId is null or option.businessAccountId = :businessAccountId) order by option.order")
    List<DataListOptionEntity> findByDataListIdAndBusinessAccountId(@Param("dataListId") UUID dataListId, @Param("businessAccountId") UUID businessAccountId);

    int countByDataListId(UUID dataListId); //todo make it businessAccount safe

    @Query(value = "from DataListOptionEntity option where option.businessAccountId is null and option.id in (:idList) order by option.order")
    List<DataListOptionEntity> findByIdIn(@Param("idList") Collection<UUID> dataListOptionId);

    @Query(value = "from DataListOptionEntity option where (option.businessAccountId is null or option.businessAccountId = :businessAccountId) and option.id in (:idList) order by option.order")
    List<DataListOptionEntity> findByIdInAndBusinessAccountId(@Param("idList") Collection<UUID> dataListOptionId, UUID businessAccountId);

    @Query(value = "from DataListOptionEntity option " +
            "where option.dataListId = :dataListId " +
            "and option.id not in (select cast(field.value as uuid) from TwinFieldSimpleEntity field where field.twinClassFieldId = :twinClassFieldId ) order by option.order")
    List<DataListOptionEntity> findByDataListIdAndNotUsedInDomain(@Param("dataListId") UUID dataListId, @Param("twinClassFieldId") UUID twinClassFieldId); //todo make it businessAccount safe

    @Query(value = "from DataListOptionEntity option " +
            "where option.dataListId = :dataListId " +
            "and option.id not in (select cast(field.value as uuid) from TwinFieldSimpleEntity field where field.twinClassFieldId = :twinClassFieldId and field.twin.ownerBusinessAccountId = :businessAccountId ) order by option.order")
    List<DataListOptionEntity> findByDataListIdAndNotUsedInBusinessAccount(@Param("dataListId") UUID dataListId, @Param("twinClassFieldId") UUID twinClassFieldId, @Param("businessAccountId") UUID businessAccountId); //todo make it businessAccount safe

    @Query(value = "from DataListOptionEntity option " +
            "where option.dataListId = :dataListId " +
            "and option.id not in (select cast(field.value as uuid) from TwinFieldSimpleEntity field where field.twinClassFieldId = :twinClassFieldId and field.twin.headTwinId = :headTwinId ) order by option.order")
    List<DataListOptionEntity> findByDataListIdAndNotUsedInHead(@Param("dataListId") UUID dataListId, @Param("twinClassFieldId") UUID twinClassFieldId, @Param("headTwinId") UUID headTwinId);

    @Query("select dlo.id from DataListOptionEntity dlo where dlo.businessAccountId = :businessAccountId and dlo.dataList.domainId = :domainId")
    List<UUID> findAllByBusinessAccountIdAndDomainId(UUID businessAccountId, UUID domainId);

}
