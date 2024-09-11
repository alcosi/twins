package org.twins.core.dao.draft;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface DraftTwinFieldUserRepository extends CrudRepository<DraftTwinFieldUserEntity, UUID>, JpaSpecificationExecutor<DraftTwinFieldUserEntity> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from draft_twin_field_user dtp " +
                    "using draft_twin_erase dte " +
                    "where dtp.draft_id = :draftId and dtp.draft_id = dte.draft_id " +
                    "and dtp.twin_id = dte.twin_id and dte.erase_twin_status_id is null " +
                    "and dtp.time_in_millis < dte.time_in_millis")
    void normalizeDraft(@Param("draftId") UUID draftId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "insert into twin_field_user (id, twin_id, twin_class_field_id, user_id) " +
                    "select gen_random_uuid(), " +
                    "       twin_id, " +
                    "       twin_class_field_id, " +
                    "       user_id " +
                    "from draft_twin_field_user " +
                    "where draft_id = :draftId " +
                    "  and cud_id = 'CREATE';")
    int commitCreates(@Param("draftId") UUID draftId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "update twin_field_user " +
                    "set user_id        = dta.user_id " +
                    "from draft_twin_field_user dta " +
                    "where draft_id = :draftId " +
                    "  and dta.twin_field_user_id = twin_field_user.id " +
                    "  and dta.cud_id = 'UPDATE';")
    int commitUpdates(@Param("draftId") UUID id);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from twin_field_user ta " +
                    "using draft_twin_field_user dta " +
                    "where dta.draft_id = :draftId " +
                    "and dta.twin_field_user_id = ta.id and cud_id = 'DELETE'")
    int commitDeletes(@Param("draftId") UUID id);
}
