package org.twins.core.dao.twin;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinCommentRepository extends CrudRepository<TwinCommentEntity, UUID>, JpaSpecificationExecutor<TwinCommentEntity> {

   @Transactional
   @Modifying
   @Query("DELETE FROM TwinCommentEntity e WHERE e.id = :id AND e.twinId = :twinId")
   void deleteByIdAndTwinId(UUID id, UUID twinId);

   List<TwinCommentEntity> findAllByTwinId(UUID id, Pageable pageable);

   long countByTwinId(UUID twinId);
}
