package org.twins.core.domain.auth;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class AuthLogin {
    private String username;
    private String password;
    private String fingerPrint;
    private UUID publicKeyId;
}
