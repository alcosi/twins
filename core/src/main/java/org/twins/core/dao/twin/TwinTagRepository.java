package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinTagRepository extends CrudRepository<TwinTagEntity, UUID>, JpaSpecificationExecutor<TwinTagEntity> {
    List<TwinTagEntity> findByTwinId(UUID twinId);
    List<TwinTagEntity> findByTwinIdIn(Set<UUID> twinIdList);
    List<TwinTagEntity> findAllByTwinIdAndTagDataListOptionIdIn(UUID twinId, Collection<UUID> tagIds);

    @Query(value = "select m.tagDataListOption from TwinTagEntity m where m.twinId = :twinId ")
    List<DataListOptionEntity> findDataListOptionByTwinId(@Param("twinId") UUID twinId);

    @Query(value = "select o from DataListOptionEntity o " +
            "where o.dataListId = :dataListId " +
            "and (o.businessAccountId = :businessAccountId or o.businessAccountId is null) " +
            "and o.id in (:idList)")
    List<DataListOptionEntity> findForBusinessAccount(@Param("dataListId") UUID dataListId, @Param("businessAccountId") UUID businessAccountId, @Param("idList") Collection<UUID> idList);

    @Query(value = "select o from DataListOptionEntity o " +
                        "where o.dataListId = :dataListId " +
                        "and o.businessAccountId is null " +
                        "and o.id in (:idList)")
    List<DataListOptionEntity> findTagsOutOfBusinessAccount(@Param("dataListId") UUID dataListId, @Param("idList") Collection<UUID> idList);

    void deleteByTwinIdAndTagDataListOptionIdIn(UUID twinId, Set<UUID> markerIdList);

    @Transactional
    void deleteByTwin_TwinClassId(UUID twinClassId);

    @Transactional
    void deleteByTwin_TwinClassIdAndTagDataListOptionIdIn(UUID twinClassId, Collection<UUID> markerIdList);

    @Query(value = "select distinct m.tagDataListOptionId from TwinTagEntity m where m.twin.twinClassId = :twinClassId ")
    Set<UUID> findDistinctTagsDataListOptionIdByTwinTwinClassId(@Param("twinClassId") UUID twinClassId);

    @Transactional
    @Modifying
    @Query(value = "update TwinTagEntity set tagDataListOptionId = :newVal where tagDataListOptionId = :oldVal and twin.twinClassId = :twinClassId")
    void replaceTagsForTwinsOfClass(@Param("twinClassId") UUID twinClassId, @Param("oldVal") UUID oldVal, @Param("newVal") UUID newVal);
}
