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
public interface DraftTwinFieldDataListRepository extends CrudRepository<DraftTwinFieldDataListEntity, UUID>, JpaSpecificationExecutor<DraftTwinFieldDataListEntity> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from draft_twin_field_data_list dtp " +
                    "using draft_twin_erase dte " +
                    "where dtp.draft_id = :draftId and dtp.draft_id = dte.draft_id " +
                    "and dtp.twin_id = dte.twin_id and dte.draft_twin_erase_status_id is null " +
                    "and dtp.time_in_millis < dte.time_in_millis")
    int normalizeDraft(@Param("draftId") UUID draftId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "insert into twin_field_data_list (id, twin_id, twin_class_field_id, data_list_option_id) " +
                    "select gen_random_uuid(), " +
                    "       twin_id, " +
                    "       twin_class_field_id, " +
                    "       data_list_option_id " +
                    "from draft_twin_field_data_list " +
                    "where draft_id = :draftId " +
                    "  and cud_id = 'CREATE';")
    int commitCreates(@Param("draftId") UUID draftId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "update twin_field_data_list " +
                    "set data_list_option_id        = dta.data_list_option_id " +
                    "from draft_twin_field_data_list dta " +
                    "where draft_id = :draftId " +
                    "  and dta.twin_field_data_list_id = twin_field_data_list.id " +
                    "  and dta.cud_id = 'UPDATE';")
    int commitUpdates(@Param("draftId") UUID id);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from twin_field_data_list ta " +
                    "using draft_twin_field_data_list dta " +
                    "where dta.draft_id = :draftId " +
                    "and dta.twin_field_data_list_id = ta.id and cud_id = 'DELETE'")
    int commitDeletes(@Param("draftId") UUID id);

    @Query(value =
            "select cud, count(*) from DraftTwinFieldDataListEntity where draftId = :draftId group by cud")
    List<Object[]> getCounters(@Param("draftId") UUID draftId);
}
