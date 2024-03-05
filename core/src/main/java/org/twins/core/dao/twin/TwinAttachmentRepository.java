package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinAttachmentRepository extends CrudRepository<TwinAttachmentEntity, UUID>, JpaSpecificationExecutor<TwinAttachmentEntity> {
    TwinAttachmentEntity getById(UUID twinId);
    List<TwinAttachmentEntity> findByTwinId(UUID twinId);
    List<TwinAttachmentEntity> findByTwinIdIn(Collection<UUID> twinIdList);
    List<TwinAttachmentEntity> findByTwinCommentIdIn(Collection<UUID> twinCommentIdList);

    List<TwinAttachmentEntity> findByTwinIdAndIdIn(UUID twinId, List<UUID> idList);

    void deleteAllByTwinIdAndIdIn(UUID twinId, List<UUID> idList);
}
