package org.twins.core.dao.twin;

import org.hibernate.query.TypedParameterValue;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinTouchRepository extends CrudRepository<TwinTouchEntity, UUID> {
    void deleteByTwinIdAndTouchIdAndUserId(UUID twinId, TwinTouchEntity.Touch touchId, UUID userId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM twin_touch WHERE twin_id = :twinId AND touch_id = :touchId", nativeQuery = true)
    void deleteByTwinIdAndTouchId(@Param("twinId") UUID twinId, @Param("touchId") String touchId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM twin_touch WHERE twin_id = :twinId AND touch_id = :touchId AND user_id = :userId", nativeQuery = true)
    void deleteByTwinIdAndTouchIdAndUserId(@Param("twinId") UUID twinId, @Param("touchId") String touchId, @Param("userId") UUID userId);

    TwinTouchEntity findByTwinIdAndTouchIdAndUserId(UUID twinId, TwinTouchEntity.Touch touchId, UUID userId);

}
