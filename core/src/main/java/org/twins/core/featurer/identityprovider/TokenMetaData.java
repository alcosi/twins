package org.twins.core.featurer.identityprovider;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TokenMetaData {
    UUID userId;
    UUID businessAccountId;
    Instant expiresAt;
}
