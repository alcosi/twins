package org.twins.core.dao.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinCommentActionAlienPermissionRepository extends JpaRepository<TwinCommentActionAlienPermissionEntity, UUID> {
    List<TwinCommentActionAlienPermissionEntity> findByTwinClassId(UUID twinClassId);

    List<TwinCommentActionAlienPermissionEntity> findByTwinClassIdIn(Collection<UUID> twinClassIds);
}
