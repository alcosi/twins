package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TwinTouchRepository extends CrudRepository<TwinTouchEntity, UUID> {
    @Transactional
    void deleteByTwinIdAndTouchId(UUID twinId, TwinTouchEntity.Touch touchId);

    @Transactional
    void deleteByTwinIdAndTouchIdAndUserId(UUID twinId, TwinTouchEntity.Touch touchId, UUID userId);

    @Query(value = "INSERT INTO twin_touch (id, twin_id, touch_id, user_id, created_at) " +
            "VALUES (:id, :twinId, :touchId, :userId, :createdAt) " +
            "ON CONFLICT (twin_id, touch_id, user_id) DO UPDATE SET id = twin_touch.id " +
            "RETURNING *", nativeQuery = true)
    Optional<TwinTouchEntity> saveOrGetIfExists(@Param("id") UUID id,
                                                @Param("twinId") UUID twinId,
                                                @Param("touchId") String touchId,
                                                @Param("userId") UUID userId,
                                                @Param("createdAt") Instant createdAt);
}
