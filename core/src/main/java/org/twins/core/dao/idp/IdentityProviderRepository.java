package org.twins.core.dao.idp;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IdentityProviderRepository extends CrudRepository<IdentityProviderEntity, UUID>, JpaSpecificationExecutor<IdentityProviderEntity> {
    <T> T findById(UUID id, Class<T> type);
}
