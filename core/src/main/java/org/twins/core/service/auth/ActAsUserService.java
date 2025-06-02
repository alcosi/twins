package org.twins.core.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CryptUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.auth.CryptKey;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class ActAsUserService  {
    private final AuthService authService;
    private final ConcurrentMap<UUID, CryptKey> domainKeysMap = new ConcurrentHashMap<>();

    public CryptKey.CryptPublicKey getPublicKey() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        CryptKey domainKey = domainKeysMap.computeIfAbsent(apiUser.getDomainId(), k -> new CryptKey().setExpires(LocalDateTime.now()));
        if (domainKey.getExpires().isBefore(LocalDateTime.now())) {
            //refresh
            try {
                domainKey.setId(UUID.randomUUID())
                        .setKeyPair(CryptUtils.generateRsaKeyPair())
                        .setExpires(LocalDateTime.now().plusMinutes(10)); //todo move to properties
            } catch (NoSuchAlgorithmException e) {
                throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION);
            }
        }
        return domainKey.getPublicKey();
    }
}
