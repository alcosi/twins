package org.twins.core.dao.attachment;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TwinAttachmentRestrictionRepository extends CrudRepository<TwinAttachmentRestrictionEntity, UUID>, JpaSpecificationExecutor<TwinAttachmentEntity> {
}
