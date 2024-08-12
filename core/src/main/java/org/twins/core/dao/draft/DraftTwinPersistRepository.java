package org.twins.core.dao.draft;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    long normalizeDraft(@Param("draftId") UUID draftId);

    Slice<DraftTwinPersistEntity> findByDraftIdAndCreateElseUpdateTrue(UUID draftId, Pageable pageable);
    Slice<DraftTwinPersistEntity> findByDraftIdAndCreateElseUpdateFalse(UUID draftId, Pageable pageable);
}
