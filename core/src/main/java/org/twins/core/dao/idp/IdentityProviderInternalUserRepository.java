package org.twins.core.dao.idp;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IdentityProviderInternalUserRepository extends CrudRepository<IdentityProviderInternalUserEntity, UUID>, JpaSpecificationExecutor<IdentityProviderInternalUserEntity> {
    IdentityProviderInternalUserEntity findByUser_Email(String email);
    IdentityProviderInternalUserEntity findByUser_EmailAndPasswordHash(String email, String passwordHash);
}
