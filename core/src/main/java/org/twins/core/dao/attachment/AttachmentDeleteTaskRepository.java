package org.twins.core.dao.attachment;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.TaskStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentDeleteTaskRepository extends CrudRepository<AttachmentDeleteTaskEntity, UUID>, JpaSpecificationExecutor<AttachmentDeleteTaskEntity> {
    List<AttachmentDeleteTaskEntity> findByStatusIn(List<TaskStatus> needStartStatuses);
}
