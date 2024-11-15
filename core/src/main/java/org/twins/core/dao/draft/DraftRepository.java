package org.twins.core.dao.draft;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface DraftRepository extends JpaRepository<DraftEntity, UUID>, JpaSpecificationExecutor<DraftEntity> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "delete from :tableName " +
                    "where draft_id = :draftId " +
                    "and twin_id in (select dte.twin_id from draft_twin_erase dte where dte.draft_id = :draftId and dte.draft_twin_erase_status_id is null)")
    void normalizeDraft(@Param("draftId") UUID draftId, @Param("tableName") String tableName);

    @Query(value = "select d from DraftEntity d where d.status in (:statusIds)")
    List<DraftEntity> findByStatusIdIn(@Param("statusIds") Collection<DraftEntity.Status> statusIds);

    @Query(value = "select d from DraftEntity d where d.status = :status and d.autoCommit = true")
    List<DraftEntity> findDraftsForCommit(@Param("status") DraftEntity.Status status);

    @Transactional
    @Modifying
    @Query(value = "update DraftEntity set status = :statusId where id = :draftId")
    void setStatus(@Param("draftId") UUID draftId, @Param("statusId") DraftEntity.Status statusId);
}
