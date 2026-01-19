package org.twins.core.dao.datalist;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

//Log calls that took more then 2 seconds
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Repository
public interface DataListOptionRepository extends CrudRepository<DataListOptionEntity, UUID>, JpaSpecificationExecutor<DataListOptionEntity> {
    String CACHE_DATA_LIST_OPTIONS = "DataListOptionRepository.findByDataListId";
    String CACHE_DATA_LIST_OPTIONS_WITH_BUSINESS_ACCOUNT = "DataListOptionRepository.findByDataListIdAndBusinessAccountId";

    @Cacheable(value = CACHE_DATA_LIST_OPTIONS, key = "{#dataListId}")
    @Query(value = "from DataListOptionEntity option where option.dataListId = :dataListId and option.businessAccountId is null order by option.order")
    List<DataListOptionEntity> findByDataListId(@Param("dataListId") UUID dataListId);

    @Query(value = "from DataListOptionEntity option where option.dataListId in (:dataListIds) and option.businessAccountId is null order by option.order")
    List<DataListOptionEntity> findByDataListIdIn(@Param("dataListIds") Set<UUID> dataListIds);

    @Cacheable(value = CACHE_DATA_LIST_OPTIONS_WITH_BUSINESS_ACCOUNT, key = "#dataListId + '' + #businessAccountId")
    @Query(value = "from DataListOptionEntity option where option.dataListId = :dataListId and (option.businessAccountId is null or option.businessAccountId = :businessAccountId) order by option.order")
    List<DataListOptionEntity> findByDataListIdAndBusinessAccountId(@Param("dataListId") UUID dataListId, @Param("businessAccountId") UUID businessAccountId);

    int countByDataListId(UUID dataListId); //todo make it businessAccount safe

    @Query(value = "from DataListOptionEntity option where option.businessAccountId is null and option.id in (:idList) order by option.order")
    List<DataListOptionEntity> findByIdIn(@Param("idList") Collection<UUID> dataListOptionId);

    @Query(value = "from DataListOptionEntity option where (option.businessAccountId is null or option.businessAccountId = :businessAccountId) and option.id in (:idList) order by option.order")
    List<DataListOptionEntity> findByIdInAndBusinessAccountId(@Param("idList") Collection<UUID> dataListOptionId, UUID businessAccountId);

    @Query(value = "from DataListOptionEntity option where option.dataListId in (:dataListIds) and (option.businessAccountId is null or option.businessAccountId = :businessAccountId) order by option.order")
    List<DataListOptionEntity> findByDataListIdInAndBusinessAccountId(Set<UUID> dataListIds, UUID businessAccountId);

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

    @Query("SELECT option.id FROM DataListOptionEntity option " +
            "WHERE option.dataListId = :dataListId " +
            "AND option.id NOT IN (SELECT CAST(field.value AS uuid) " +
            "FROM TwinFieldSimpleEntity field " +
            "WHERE field.twinClassFieldId = :twinClassFieldId " +
            "AND field.twin.headTwinId = :headTwinId) " +
            "ORDER BY option.order")
    Set<UUID> findOptionIdsByDataListIdAndNotUsedInHead(@Param("dataListId") UUID dataListId, @Param("twinClassFieldId") UUID twinClassFieldId,@Param("headTwinId") UUID headTwinId);

    @Query("select dlo.id from DataListOptionEntity dlo where dlo.businessAccountId = :businessAccountId and dlo.dataList.domainId = :domainId")
    List<UUID> findAllByBusinessAccountIdAndDomainId(UUID businessAccountId, UUID domainId);

    @Query(value = "SELECT o FROM DataListOptionEntity o " +
            "WHERE o.dataListId = :dataListId " +
            "AND (o.businessAccountId IS NULL) " +
            "AND (lower(:name) like lower(o.option)) " +
            "ORDER BY o.businessAccountId")
    List<DataListOptionEntity> findOptionOutOfBusinessAccount(@Param("dataListId") UUID dataListId, @Param("name") String name, Pageable pageable);

    @Query(value = "SELECT o FROM DataListOptionEntity o " +
            "WHERE o.dataListId = :dataListId " +
            "AND (o.businessAccountId = :businessAccountId OR o.businessAccountId IS NULL) " +
            "AND (lower(:name) like lower(o.option)) " +
            "ORDER BY o.businessAccountId")
    List<DataListOptionEntity> findOptionForBusinessAccount(@Param("dataListId") UUID dataListId, @Param("businessAccountId") UUID businessAccountId, @Param("name") String name, Pageable pageable);
}
