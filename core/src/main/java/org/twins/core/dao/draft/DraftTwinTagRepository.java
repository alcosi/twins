package org.twins.core.dao.draft;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DraftTwinTagRepository extends CrudRepository<DraftTwinTagEntity, UUID>, JpaSpecificationExecutor<DraftTwinTagEntity> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from draft_twin_tag dtp " +
                    "using draft_twin_erase dte " +
                    "where dtp.draft_id = :draftId and dtp.draft_id = dte.draft_id " +
                    "and dtp.twin_id = dte.twin_id and dte.erase_twin_status_id is null " +
                    "and dtp.time_in_millis < dte.time_in_millis")
    void normalizeDraft(@Param("draftId") UUID draftId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "insert into twin_tag (id, twin_id, tag_data_list_option_id, created_at) " +
                    "select gen_random_uuid(), twin_id, tag_data_list_option_id, now() " +
                    "from draft_twin_tag where draft_id = :draftId and create_else_delete = true")
    void commitTagsAdd(@Param("draftId") UUID draftId);


    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from twin_tag tt " +
                    "using draft_twin_tag dtt where tt.twin_id = dtt.twin_id and tt.tag_data_list_option_id = dtt.tag_data_list_option_id and dtt.draft_id = :draftId and dtt.create_else_delete = false")
    void commitTagsDelete(@Param("draftId") UUID draftId);
}
