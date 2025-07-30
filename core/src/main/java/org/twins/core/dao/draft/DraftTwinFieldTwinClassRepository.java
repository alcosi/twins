package org.twins.core.dao.draft;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface DraftTwinFieldTwinClassRepository extends CrudRepository<DraftTwinFieldTwinClassEntity, UUID>, JpaSpecificationExecutor<DraftTwinFieldTwinClassEntity> {

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from draft_twin_field_twin_class dtftc " +
                    "using draft_twin_erase dte " +
                    "where dtftc.draft_id = :draftId and dtftc.draft_id = dte.draft_id " +
                    "and dtftc.twin_id = dte.twin_id and dte.draft_twin_erase_status_id is null " +
                    "and dtftc.time_in_millis < dte.time_in_millis")
    int normalizeDraft(@Param("draftId") UUID draftId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "insert into twin_field_twin_class (id, twin_id, twin_class_field_id, twin_class_id) " +
                    "select gen_random_uuid(), " +
                    "       twin_id, " +
                    "       twin_class_field_id, " +
                    "       twin_class_id " +
                    "from draft_twin_field_twin_class " +
                    "where draft_id = :draftId " +
                    "  and cud_id = 'CREATE';")
    int commitCreates(@Param("draftId") UUID draftId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "update twin_field_twin_class tftc " +
                    "set twin_class_id = dtftc.twin_class_id " +
                    "from draft_twin_field_twin_class dtftc " +
                    "where draft_id = :draftId " +
                    "  and dtftc.twin_field_twin_class_id = tftc.id " +
                    "  and dtftc.cud_id = 'UPDATE';")
    int commitUpdates(@Param("draftId") UUID id);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from twin_field_twin_class tftc " +
                    "using draft_twin_field_twin_class dtftc " +
                    "where dtftc.draft_id = :draftId " +
                    "and dtftc.twin_field_twin_class_id = tftc.id and cud_id = 'DELETE'")
    int commitDeletes(@Param("draftId") UUID id);

    @Query(value =
            "select cud, count(*) from DraftTwinFieldTwinClassEntity where draftId = :draftId group by cud")
    List<Object[]> getCounters(@Param("draftId") UUID draftId);
}
