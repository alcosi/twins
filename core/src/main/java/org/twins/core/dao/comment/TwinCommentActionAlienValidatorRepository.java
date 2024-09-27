package org.twins.core.dao.comment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.action.TwinActionValidatorEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinCommentActionAlienValidatorRepository extends CrudRepository<TwinCommentActionAlienValidatorEntity, UUID> {
    List<TwinCommentActionAlienValidatorEntity> findByTwinClassIdOrderByOrder(UUID twinClassId);

    List<TwinCommentActionAlienValidatorEntity> findByTwinClassIdIn(Set<UUID> twinClassIds);
}
