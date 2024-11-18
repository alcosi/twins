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
public interface DraftHistoryRepository extends CrudRepository<DraftHistoryEntity, UUID>, JpaSpecificationExecutor<DraftHistoryEntity> {
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "insert into public.history (id, twin_id, created_at, actor_user_id, history_type_id, twin_class_field_id, context, snapshot_message, history_batch_id) " +
            "select id, twin_id, current_timestamp, actor_user_id, history_type_id, twin_class_field_id, context, snapshot_message, draft_id from draft_history where draft_id = :draftId")
    int moveFromDraft(@Param("draftId") UUID draftId);
}
