package org.twins.core.dao.businessaccount;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.user.UserEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface BusinessAccountUserRepository extends CrudRepository<BusinessAccountUserEntity, UUID>, JpaSpecificationExecutor<BusinessAccountUserEntity> {
    <T> T findByBusinessAccountIdAndUserId(UUID businessAccountId, UUID userId, Class<T> type);

    @Query(value = "select bu, bu.businessAccountSpecOnly, bu.userSpecOnly from BusinessAccountUserEntity bu where bu.businessAccountId = :businessAccountId and bu.userId = :userId")
    List<Object[]> _findByBusinessAccountIdAndUserId(UUID businessAccountId, UUID userId);

    default BusinessAccountUserEntity findByBusinessAccountIdAndUserId(UUID businessAccountId, UUID userId) {
        var results = _findByBusinessAccountIdAndUserId(businessAccountId, userId);
        if (results == null)
            return null;
        var row = results.getFirst();
        var ret = (BusinessAccountUserEntity) row[0];
        ret
                .setBusinessAccount((BusinessAccountEntity) row[1])
                .setUser((UserEntity) row[2]);
        return ret;
    }

    List<BusinessAccountUserEntity> findByUserIdIn(Set<UUID> users);

    List<BusinessAccountUserEntity> findByBusinessAccountId(UUID businessAccountId);

    @Query("select bau.userId from BusinessAccountUserEntity bau where bau.businessAccountId = :businessAccountId")
    List<UUID> findUserIdByBusinessAccountId(@Param("businessAccountId") UUID businessAccountId);

    @Modifying
    @Query("UPDATE BusinessAccountUserEntity e SET e.lastActivityAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void updateLastActivityAt(@Param("id") UUID id);
}
