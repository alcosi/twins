package org.twins.core.dao.twin;

import org.hibernate.query.TypedParameterValue;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinTouchRepository extends CrudRepository<TwinTouchEntity, UUID> {
    void deleteByTwinIdAndTouchIdAndUserId(UUID twinId, TwinTouchEntity.Touch touchId, UUID userId);

    TwinTouchEntity findByTwinIdAndTouchIdAndUserId(UUID twinId, TwinTouchEntity.Touch touchId, UUID userId);

}
