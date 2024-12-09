package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TwinTouchRepository extends CrudRepository<TwinTouchEntity, UUID> {
    @Transactional
    void deleteByTwinIdAndTouchId(UUID twinId, TwinTouchEntity.Touch touchId);

    @Transactional
    void deleteByTwinIdAndTouchIdAndUserId(UUID twinId, TwinTouchEntity.Touch touchId, UUID userId);

    List<TwinTouchEntity> findByTwinIdInAndTouchIdAndUserId(Collection<UUID> twinIds, TwinTouchEntity.Touch touchId, UUID userId);
}
