package org.twins.core.dao.businessaccount;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface BusinessAccountUserRepository extends CrudRepository<BusinessAccountUserEntity, UUID>, JpaSpecificationExecutor<BusinessAccountUserEntity> {
    <T> T findByBusinessAccountIdAndUserId(UUID businessAccountId, UUID userId, Class<T> type);

    List<BusinessAccountUserEntity> findByUserIdIn(Set<UUID> users);
}
