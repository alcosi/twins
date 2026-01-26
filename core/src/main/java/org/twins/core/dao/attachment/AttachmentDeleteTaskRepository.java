package org.twins.core.dao.attachment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.enums.attachment.AttachmentDeleteTaskStatus;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentDeleteTaskRepository extends CrudRepository<AttachmentDeleteTaskEntity, UUID>, JpaSpecificationExecutor<AttachmentDeleteTaskEntity> {
    List<AttachmentDeleteTaskEntity> findByStatusIn(List<AttachmentDeleteTaskStatus> statuses);
    List<AttachmentDeleteTaskEntity> findByStatusIn(List<AttachmentDeleteTaskStatus> statuses, Pageable pageable);

    void deleteAllByStatusIn(List<AttachmentDeleteTaskStatus> statuses);
    long countAllByStatusIn(List<AttachmentDeleteTaskStatus> statuses);
    void deleteAllByStatusInAndCreatedAtBefore(List<AttachmentDeleteTaskStatus> needStartStatuses, Timestamp createdAt);
    long countAllByStatusInAndCreatedAtBefore(List<AttachmentDeleteTaskStatus> needStartStatuses, Timestamp createdAt);
}
