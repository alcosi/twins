package org.twins.core.dao.twin;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.enums.twin.Touch;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinTouchRepository extends CrudRepository<TwinTouchEntity, UUID> {
    @Transactional
    void deleteByTwinIdAndTouchId(UUID twinId, Touch touchId);

    @Transactional
    void deleteByTwinIdAndTouchIdAndUserId(UUID twinId, Touch touchId, UUID userId);

    List<TwinTouchEntity> findByTwinIdInAndTouchIdAndUserId(Collection<UUID> twinIds, Touch touchId, UUID userId);
}
