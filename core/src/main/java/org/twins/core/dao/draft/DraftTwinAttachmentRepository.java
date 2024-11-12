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
                    "and dtp.twin_id = dte.twin_id and dte.draft_twin_erase_status_id is null " +
                    "and dtp.time_in_millis < dte.time_in_millis")
    int normalizeDraft(@Param("draftId") UUID draftId);

    Slice<DraftTwinAttachmentEntity> findByDraftIdAndCud(UUID draftId, CUD cud, Pageable pageable);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "insert into twin_attachment (id, twin_id, twinflow_transition_id, storage_link, view_permission_id, created_by_user_id, " +
                    "                             created_at, external_id, title, description, twin_comment_id, twin_class_field_id) " +
                    "select gen_random_uuid (), " +
                    "       twin_id, " +
                    "       twinflow_transition_id, " +
                    "       storage_link, view_permission_id, " +
                    "       created_by_user_id, " +
                    "       current_timestamp, " +
                    "       external_id, " +
                    "       title, " +
                    "       description,  " +
                    "       twin_comment_id, " +
                    "       twin_class_field_id " +
                    "       from draft_twin_attachment " +
                    "where draft_id = :draftId " +
                    "  and cud_id = 'CREATE';")
    int commitAttachmentsCreate(UUID draftId);


    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "update twin_attachment " +
                    "set storage_link        = nullifyIfNecessary(dta.storage_link, storage_link), " +
                    "    view_permission_id  = nullifyIfNecessary(dta.view_permission_id,view_permission_id), " +
                    "    external_id         = nullifyIfNecessary(dta.external_id,external_id), " +
                    "    title               = nullifyIfNecessary(dta.title,title), " +
                    "    description         = nullifyIfNecessary(dta.description,description), " +
                    "    twin_class_field_id = nullifyIfNecessary(dta.twin_class_field_id,twin_class_field_id), " +
                    "    twin_comment_id     = nullifyIfNecessary(dta.twin_comment_id, twin_comment_id) " +
                    "from draft_twin_attachment dta " +
                    "where draft_id = :draftId " +
                    "  and dta.twin_attachment_id = twin_attachment.id " +
                    "  and dta.cud_id = 'UPDATE';")
    int commitAttachmentsUpdateDelta(@Param("draftId") UUID draftId); //todo use me if only delta will stored in db

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "update twin_attachment " +
                    "set storage_link        = dta.storage_link, " +
                    "    view_permission_id  = dta.view_permission_id, " +
                    "    external_id         = dta.external_id, " +
                    "    title               = dta.title, " +
                    "    description         = dta.description, " +
                    "    twin_class_field_id = dta.twin_class_field_id, " +
                    "    twin_comment_id     = dta.twin_comment_id " +
                    "from draft_twin_attachment dta " +
                    "where draft_id = :draftId " +
                    "  and dta.twin_attachment_id = twin_attachment.id " +
                    "  and dta.cud_id = 'UPDATE';")
    int commitAttachmentsUpdate(@Param("draftId") UUID draftId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from twin_attachment ta " +
                    "using draft_twin_attachment dta " +
                    "where dta.draft_id = :draftId " +
                    "and dta.twin_attachment_id = ta.id and cud_id = 'DELETE'")
    int commitAttachmentsDelete(@Param("draftId") UUID draftId);
}
