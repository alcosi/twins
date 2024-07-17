package org.twins.core.dao.twin;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinEraserTransactionScopeRepository extends CrudRepository<TwinEraserTransactionScopeEntity, TwinEraserTransactionScopeEntity.PK>, JpaSpecificationExecutor<TwinEraserTransactionScopeEntity> {

   @Transactional
   @Modifying
   @Query(nativeQuery = true, value =
           "insert into twin_eraser_transaction_scope (twin_eraser_transaction_id, twin_id, self_scope_loaded, reason_twin_id, twin_eraser_reason_id) " +
           "select :transactionId, id, false, :twinId, 'CHILD' from twin where hierarchy_tree ~ ('*.' || :twinId || '.*')::lquery " +
           "on conflict do nothing;")
   void addChildTwins(@Param("transactionId") UUID transactionId, @Param("twinId") UUID twinId);

   //todo
   void addLinked(@Param("transactionId") UUID transactionId, @Param("twinId") UUID twinId);

   boolean existsByTwinEraserTransactionIdAndSelfScopeLoadedFalse(UUID transactionId);

   List<TwinEraserTransactionScopeEntity> findByTwinEraserTransactionIdAndSelfScopeLoadedFalse(UUID transactionId);
}
