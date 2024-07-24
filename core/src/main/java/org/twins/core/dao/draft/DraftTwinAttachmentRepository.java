package org.twins.core.dao.draft;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DraftTwinAttachmentRepository extends CrudRepository<DraftTwinAttachmentEntity, UUID>, JpaSpecificationExecutor<DraftTwinAttachmentEntity> {
}
