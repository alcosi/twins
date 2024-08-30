package org.twins.core.dao.draft;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DraftTwinMarkerRepository extends CrudRepository<DraftTwinMarkerEntity, UUID>, JpaSpecificationExecutor<DraftTwinMarkerEntity> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from draft_twin_marker dtp " +
                    "using draft_twin_erase dte " +
                    "where dtp.draft_id = :draftId and dtp.draft_id = dte.draft_id " +
                    "and dtp.twin_id = dte.twin_id and dte.erase_twin_status_id is null " +
                    "and dtp.time_in_millis < dte.time_in_millis")
    void normalizeDraftByTwinDeletion(@Param("draftId") UUID draftId);

    Slice<DraftTwinMarkerEntity> findByDraftId(UUID draftId, PageRequest of);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "insert into twin_marker (id, twin_id, marker_data_list_option_id, created_at) " +
                    "select gen_random_uuid(), twin_id, marker_data_list_option_id, now() " +
                    "from draft_twin_marker where draft_id = :draftId and create_else_delete = true")
    long commitMarkersAdd(UUID draftId);


    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from twin_marker tm " +
                    "using draft_twin_marker dtm where tm.twin_id = dtm.twin_id and tm.marker_data_list_option_id = dtm.marker_data_list_option_id and dtm.draft_id = :draftId and dtm.create_else_delete = false")
    long commitMarkersDelete(UUID draftId);
}
