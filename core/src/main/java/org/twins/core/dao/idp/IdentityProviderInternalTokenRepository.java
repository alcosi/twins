package org.twins.core.dao.idp;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IdentityProviderInternalTokenRepository extends CrudRepository<IdentityProviderInternalTokenEntity, UUID>, JpaSpecificationExecutor<IdentityProviderInternalTokenEntity> {
    <T> T findById(UUID id, Class<T> type);

    IdentityProviderInternalTokenEntity findByRefreshToken(String refreshToken);
    IdentityProviderInternalTokenEntity findByAccessToken(String accessToken);
    IdentityProviderInternalTokenEntity findByRefreshTokenAndFingerPrint(String refreshToken, String fingerPrint);
}
