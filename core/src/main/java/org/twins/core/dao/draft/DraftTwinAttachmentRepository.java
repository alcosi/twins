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
import org.twins.core.dao.CUD;

import java.util.UUID;

@Repository
public interface DraftTwinAttachmentRepository extends CrudRepository<DraftTwinAttachmentEntity, UUID>, JpaSpecificationExecutor<DraftTwinAttachmentEntity> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from draft_twin_attachment dtp " +
                    "using draft_twin_erase dte " +
                    "where dtp.draft_id = :draftId and dtp.draft_id = dte.draft_id " +
                    "and dtp.twin_id = dte.twin_id and dte.erase_twin_status_id is null " +
                    "and dtp.time_in_millis < dte.time_in_millis")
    void normalizeDraft(@Param("draftId") UUID draftId);

    Slice<DraftTwinAttachmentEntity> findByDraftIdAndCud(UUID draftId, CUD cud, Pageable pageable);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from twin_attachment where id in (select twin_attachment_id from draft_twin_attachment where draft_id = :draftId and draft_twin_attachment.cud_id = 'DELETE')")
    void commitAttachmentDelete(@Param("draftId") UUID draftId);
}
