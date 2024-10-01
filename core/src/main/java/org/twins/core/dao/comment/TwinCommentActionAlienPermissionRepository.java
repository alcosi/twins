package org.twins.core.dao.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.action.TwinActionPermissionEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinCommentActionAlienPermissionRepository extends JpaRepository<TwinCommentActionAlienPermissionEntity, UUID> {
    List<TwinCommentActionAlienPermissionEntity> findByTwinClassId(UUID twinClassId);

    List<TwinCommentActionAlienPermissionEntity> findByTwinClassIdIn(Set<UUID> twinClassIds);
}
