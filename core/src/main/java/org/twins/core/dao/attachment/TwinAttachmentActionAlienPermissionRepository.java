package org.twins.core.dao.attachment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinAttachmentActionAlienPermissionRepository extends CrudRepository<TwinAttachmentActionAlienPermissionEntity, UUID> {
    List<TwinAttachmentActionAlienPermissionEntity> findByTwinClassId(UUID twinClassId);

    List<TwinAttachmentActionAlienPermissionEntity> findByTwinClassIdIn(Collection<UUID> twinClassIds);
}
