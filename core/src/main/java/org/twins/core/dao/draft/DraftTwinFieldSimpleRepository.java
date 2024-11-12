package org.twins.core.dao.draft;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.CUD;

import java.util.UUID;

@Repository
public interface DraftTwinFieldSimpleRepository extends CrudRepository<DraftTwinFieldSimpleEntity, UUID>, JpaSpecificationExecutor<DraftTwinFieldSimpleEntity> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from draft_twin_field_simple dtp " +
                    "using draft_twin_erase dte " +
                    "where dtp.draft_id = :draftId and dtp.draft_id = dte.draft_id " +
                    "and dtp.twin_id = dte.twin_id and dte.draft_twin_erase_status_id is null " +
                    "and dtp.time_in_millis < dte.time_in_millis")
    int normalizeDraft(@Param("draftId") UUID draftId);

    Slice<DraftTwinFieldSimpleEntity> findByDraftIdAndCud(UUID id, CUD cud, PageRequest of);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "insert into twin_field_simple (id, twin_id, twin_class_field_id, value) " +
                    "select gen_random_uuid(), " +
                    "       twin_id, " +
                    "       twin_class_field_id, " +
                    "       value " +
                    "from draft_twin_field_simple " +
                    "where draft_id = :draftId " +
                    "  and cud_id = 'CREATE';")
    int commitCreates(@Param("draftId") UUID id);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "update twin_field_simple " +
                    "set value        = dta.value " +
                    "from draft_twin_field_simple dta " +
                    "where draft_id = :draftId " +
                    "  and dta.twin_field_simple_id = twin_field_simple.id " +
                    "  and dta.cud_id = 'UPDATE';")
    int commitUpdates(@Param("draftId") UUID id);
}

