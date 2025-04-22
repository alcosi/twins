package org.twins.core.dao.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public interface TwinCommentRepository extends CrudRepository<TwinCommentEntity, UUID>, JpaSpecificationExecutor<TwinCommentEntity> {

   Page<TwinCommentEntity> findAllByTwinId(UUID id, Pageable pageable);

   Optional<TwinCommentEntity> findByTwinIdAndCreatedByUserId(UUID twinId, UUID createdByUserId);

   List<TwinCommentEntity> findByIdIn(Collection<UUID> ids);
}
