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
public interface DraftTwinLinkRepository extends CrudRepository<DraftTwinLinkEntity, UUID>, JpaSpecificationExecutor<DraftTwinLinkEntity> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from draft_twin_link dtp " +
                    "using draft_twin_erase dte " +
                    "where dtp.draft_id = :draftId and dtp.draft_id = dte.draft_id " +
                    "and dtp.dst_twin_id = dte.twin_id and dte.draft_twin_erase_status_id is null " +
                    "and dtp.cud_id = 'DELETE'")
    int normalizeDraft(@Param("draftId") UUID draftId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "insert into twin_link (id, src_twin_id, dst_twin_id, link_id, created_by_user_id)  " +
                    "select gen_random_uuid(), " +
                    "       src_twin_id, " +
                    "       dst_twin_id, " +
                    "       link_id, " +
                    "       created_by_user_id " +
                    "from draft_twin_link " +
                    "where draft_id = :draftId " +
                    "  and cud_id = 'CREATE';")
    int commitCreates(@Param("draftId") UUID draftId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "update twin_link " +
                    "set src_twin_id = dtl.src_twin_id, " +
                    "dst_twin_id = dtl.dst_twin_id " +
                    "from draft_twin_link dtl " +
                    "where draft_id = :draftId " +
                    "  and dtl.twin_link_id = twin_link.id " +
                    "  and dtl.cud_id = 'UPDATE';")
    int commitUpdates(@Param("draftId") UUID id);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from twin_link ta " +
                    "using draft_twin_link dtl " +
                    "where dtl.draft_id = :draftId " +
                    "and dtl.twin_link_id = ta.id and cud_id = 'DELETE'")
    int commitDeletes(@Param("draftId") UUID id);
}
