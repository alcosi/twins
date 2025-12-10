package org.twins.core.dao.draft;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.enums.draft.DraftStatus;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface DraftRepository extends JpaRepository<DraftEntity, UUID>, JpaSpecificationExecutor<DraftEntity> {

    List<DraftEntity> findByStatusIn(Collection<DraftStatus> statusIds);
    List<DraftEntity> findByStatusIn(Collection<DraftStatus> statusIds, Pageable pageable);
    List<DraftEntity> findByStatusInAndAutoCommit(List<DraftStatus> statusIds, boolean autoCommit);
    List<DraftEntity> findByStatusInAndAutoCommit(List<DraftStatus> statusIds, boolean autoCommit, Pageable pageable);

    @Transactional
    @Modifying
    @Query(value = "update DraftEntity set status = :statusId where id = :draftId")
    void setStatus(@Param("draftId") UUID draftId, @Param("statusId") DraftStatus statusId);
}
