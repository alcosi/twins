package org.twins.core.dao.attachment;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinAttachmentModificationRepository extends CrudRepository<TwinAttachmentModificationEntity, UUID>, JpaSpecificationExecutor<TwinAttachmentModificationEntity> {

    List<TwinAttachmentModificationEntity> findAllByTwinAttachmentIdIn(Collection<UUID> ids);

}
