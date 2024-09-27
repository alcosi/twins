package org.twins.core.dao.comment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinCommentActionSelfRepository extends CrudRepository<TwinCommentActionSelfEntity, UUID> {
    List<TwinCommentActionSelfEntity> findByTwinClassId(UUID twinClassId);

    List<TwinCommentActionSelfEntity> findByTwinClassIdIn(Set<UUID> twinClassIds);
}
