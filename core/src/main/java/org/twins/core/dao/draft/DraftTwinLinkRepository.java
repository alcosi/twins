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
public interface DraftTwinLinkRepository extends CrudRepository<DraftTwinLinkEntity, UUID>, JpaSpecificationExecutor<DraftTwinLinkEntity> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from draft_twin_link dtp " +
                    "using draft_twin_erase dte " +
                    "where dtp.draft_id = :draftId and dtp.draft_id = dte.draft_id " +
                    "and dtp.dst_twin_id = dte.twin_id and dte.erase_twin_status_id is null " +
                    "and dtp.cud_id = 'DELETE'")
    void normalizeDraft(@Param("draftId") UUID draftId);
}
