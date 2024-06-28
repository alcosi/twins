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
public interface TwinMarkerRepository extends CrudRepository<TwinMarkerEntity, UUID>, JpaSpecificationExecutor<TwinMarkerEntity> {
    List<TwinMarkerEntity> findByTwinId(UUID twinId);
    List<TwinMarkerEntity> findByTwinIdIn(Set<UUID> twinIdList);

    @Query(value = "select m.markerDataListOption from TwinMarkerEntity m where m.twinId = :twinId ")
    List<DataListOptionEntity> findDataListOptionByTwinId(@Param("twinId") UUID twinId);

    void deleteByTwinId(UUID twinId);

    void deleteByTwinIdAndMarkerDataListOptionIdIn(UUID twinId, Set<UUID> markerIdList);

    @Transactional
    void deleteByTwin_TwinClassId(UUID twinClassId);

    @Transactional
    void deleteByTwin_TwinClassIdAndMarkerDataListOptionIdIn(UUID twinClassId, Collection<UUID> markerIdList);

    @Query(value = "select distinct m.markerDataListOptionId from TwinMarkerEntity m where m.twin.twinClassId = :twinClassId ")
    Set<UUID> findDistinctMakersDataListOptionIdByTwinTwinClassId(@Param("twinClassId") UUID twinClassId);

//    @Query(value = "UPDATE twin_marker SET marker_data_list_option_id = replacement.new_val" +
//            " FROM (VALUES :values AS replacement (old_val, new_val) WHERE twin_marker.marker_data_list_option_id = replacement.old_val", nativeQuery = true)
    @Transactional
    @Modifying
    @Query(value = "update TwinMarkerEntity set markerDataListOptionId = :newVal where markerDataListOptionId = :oldVal and twin.twinClassId = :twinClassId")
    void replaceMarkersForTwinsOfClass(@Param("twinClassId") UUID twinClassId, @Param("oldVal") UUID oldVal, @Param("newVal") UUID newVal);
}
