package org.twins.core.domain.apiuser;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class ActAsUser {
    UUID userId;
    UUID businessAccountId;
}
