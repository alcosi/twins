package org.twins.core.dao.businessaccount;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface BusinessAccountUserRepository extends CrudRepository<BusinessAccountUserEntity, UUID>, JpaSpecificationExecutor<BusinessAccountUserEntity> {
    <T> T findByBusinessAccountIdAndUserId(UUID businessAccountId, UUID userId, Class<T> type);

    List<BusinessAccountUserEntity> findByUserIdIn(Set<UUID> users);

    List<BusinessAccountUserEntity> findByBusinessAccountId(UUID businessAccountId);

    @Query("select bau.userId from BusinessAccountUserEntity bau where bau.businessAccountId = :businessAccountId")
    List<UUID> findUserIdByBusinessAccountId(@Param("businessAccountId") UUID businessAccountId);

    @Modifying
    @Query("UPDATE BusinessAccountUserEntity e SET e.lastActivityAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void updateLastActivityAt(@Param("id") UUID id);
}
