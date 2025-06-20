package org.twins.core.dao.draft;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.CUD;

import java.util.List;
import java.util.UUID;

public interface DraftTwinFieldSimpleNonIndexedRepository extends CrudRepository<DraftTwinFieldSimpleNonIndexedEntity, UUID>, JpaSpecificationExecutor<DraftTwinFieldSimpleNonIndexedEntity> {

    Slice<DraftTwinFieldSimpleNonIndexedEntity> findByDraftIdAndCud(UUID id, CUD cud, PageRequest of);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from draft_twin_field_simple_non_indexed dtfsni" +
                    "using draft_twin_erase dte " +
                    "where dtfsni.draft_id = :draftId and dtfsni.draft_id = dte.draft_id " +
                    "and dtfsni.twin_id = dte.twin_id and dte.draft_twin_erase_status_id is null " +
                    "and dtfsni.time_in_millis < dte.time_in_millis")
    int normalizeDraft(@Param("draftId") UUID draftId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "insert into twin_field_simple_non_indexed (id, twin_id, twin_class_field_id, value) " +
                    "select gen_random_uuid(), " +
                    "       twin_id, " +
                    "       twin_class_field_id, " +
                    "       value " +
                    "from draft_twin_field_simple_non_indexed " +
                    "where draft_id = :draftId " +
                    "  and cud_id = 'CREATE';")
    int commitCreates(@Param("draftId") UUID id);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "update twin_field_simple_non_indexed " +
                    "set value        = dta.value " +
                    "from draft_twin_field_simple_non_indexed dta " +
                    "where draft_id = :draftId " +
                    "  and dta.twin_field_simple_non_indexed_id = twin_field_simple_non_indexed.id " +
                    "  and dta.cud_id = 'UPDATE';")
    int commitUpdates(@Param("draftId") UUID id);

    @Query(value = "select cud, count(*) from DraftTwinFieldSimpleNonIndexedEntity where draftId = :draftId group by cud")
    List<Object[]> getCounters(@Param("draftId") UUID draftId);
}
