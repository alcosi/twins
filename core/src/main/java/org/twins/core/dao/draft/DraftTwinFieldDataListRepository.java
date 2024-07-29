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
public interface DraftTwinFieldDataListRepository extends CrudRepository<DraftTwinFieldDataListEntity, UUID>, JpaSpecificationExecutor<DraftTwinFieldDataListEntity> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from draft_twin_field_data_list " +
                    "where draft_id = :draftId " +
                    "and twin_id in (select id from draft_twin_erase dte where dte.draft_id = :draftId and dte.erase_twin_status_id is null)")
    void normalizeDraft(@Param("draftId") UUID draftId);
}
