
package org.twins.core.dao.twin;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinAttachmentActionSelfRepository extends CrudRepository<TwinAttachmentActionSelfEntity, UUID> {
    List<TwinAttachmentActionSelfEntity> findByTwinClassId(UUID twinClassId);

    List<TwinAttachmentActionSelfEntity> findByTwinClassIdIn(Collection<UUID> twinClassIds);
}
