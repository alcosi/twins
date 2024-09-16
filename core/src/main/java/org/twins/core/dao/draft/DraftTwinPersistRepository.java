package org.twins.core.dao.draft;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface DraftTwinPersistRepository extends CrudRepository<DraftTwinPersistEntity, UUID>, JpaSpecificationExecutor<DraftTwinPersistEntity> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from draft_twin_persist dtp " +
                    "using draft_twin_erase dte " +
                    "where dtp.draft_id = :draftId and dtp.draft_id = dte.draft_id " +
                    "and dtp.twin_id = dte.twin_id and dte.erase_twin_status_id is null " +
                    "and dtp.time_in_millis < dte.time_in_millis")
    int normalizeDraft(@Param("draftId") UUID draftId);

    Slice<DraftTwinPersistEntity> findByDraftIdAndCreateElseUpdateTrue(UUID draftId, Pageable pageable);
    Slice<DraftTwinPersistEntity> findByDraftIdAndCreateElseUpdateFalse(UUID draftId, Pageable pageable);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "update twin " +
                    "set twin_status_id     = nullifyIfNecessary(dtp.twin_status_id, twin_status_id), " +
                    "    head_twin_id       = nullifyIfNecessary(dtp.head_twin_id, head_twin_id), " +
                    "    external_id        = nullifyIfNecessary(dtp.external_id, external_id), " +
                    "    name               = nullifyIfNecessary(dtp.name, name), " +
                    "    description        = nullifyIfNecessary(dtp.description, description), " +
                    "    assigner_user_id   = nullifyIfNecessary(dtp.assigner_user_id, assigner_user_id), " +
                    "    view_permission_id = nullifyIfNecessary(dtp.view_permission_id, view_permission_id) " +
                    "from draft_twin_persist dtp " +
                    "where draft_id = :draftId " +
                    "  and dtp.twin_id = twin.id " +
                    "  and dtp.create_else_update = false;")
    int commitTwinsUpdates(UUID draftId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "insert into twin (id, twin_class_id, head_twin_id, external_id, twin_status_id, name, description, created_by_user_id, " +
                    "                  assigner_user_id, created_at, owner_business_account_id, owner_user_id, view_permission_id) " +
                    "select twin_id, " +
                    "       twin_class_id, " +
                    "       head_twin_id, " +
                    "       external_id, " +
                    "       twin_status_id, " +
                    "       name, " +
                    "       description, " +
                    "       created_by_user_id, " +
                    "       assigner_user_id, " +
                    "       current_timestamp, " +
                    "       owner_business_account_id, " +
                    "       owner_user_id, " +
                    "       view_permission_id " +
                    "from draft_twin_persist " +
                    "where draft_id = :draftId " +
                    "  and create_else_update = true;")
    int commitTwinsCreates(UUID draftId);
}
