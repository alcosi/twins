package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinMarkerRepository extends CrudRepository<TwinMarkerEntity, UUID>, JpaSpecificationExecutor<TwinMarkerEntity> {
    List<TwinMarkerEntity> findByTwinId(UUID twinId);

    @Query(value = "select m.markerDataListOption from TwinMarkerEntity m where m.twinId = :twinId ")
    List<DataListOptionEntity> findDataListOptionByTwinId(@Param("twinId") UUID twinId);

    void deleteByTwinId(UUID twinId);
}
