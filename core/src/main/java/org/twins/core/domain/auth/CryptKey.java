package org.twins.core.domain.auth;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CryptUtils;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Accessors(chain = true)
public class CryptKey {
    private UUID id;
    private KeyPair keyPair;
    private LocalDateTime expires;
    private Set<UUID> nonceSet = ConcurrentHashMap.newKeySet();

    public CryptPublicKey getPublicKey() {
        return new CryptPublicKey()
                .setId(id)
                .setPublicKey(keyPair.getPublic())
                .setExpires(expires);
    }

    public synchronized void flush() throws NoSuchAlgorithmException {
        id = UuidCreator.getTimeOrdered();
        keyPair = CryptUtils.generateRsaKeyPair();
        expires = LocalDateTime.now().plusMinutes(10);
        nonceSet.clear();
    }

    public boolean isAlreadyProcessed(UUID nonce) {
        if (nonceSet.contains(nonce)) {
            return true;
        }
        nonceSet.add(nonce);
        return false;
    }

    @Data
    @Accessors(chain = true)
    public static class CryptPublicKey {
        private UUID id;
        private PublicKey publicKey;
        private LocalDateTime expires;
    }
}
