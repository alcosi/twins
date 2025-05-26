package org.twins.core.domain.auth;

import lombok.Data;
import lombok.experimental.Accessors;

import java.security.KeyPair;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class LoginKey {
    private UUID id;
    private KeyPair keyPair;
    private LocalDateTime expires;

    public LoginPublicKey getPublicKey() {
        return new LoginPublicKey()
                .setId(id)
                .setPublicKey(keyPair.getPublic())
                .setExpires(expires);
    }

    @Data
    @Accessors(chain = true)
    public static class LoginPublicKey {
        private UUID id;
        private PublicKey publicKey;
        private LocalDateTime expires;
    }
}
