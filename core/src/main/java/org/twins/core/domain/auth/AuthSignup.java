package org.twins.core.domain.auth;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class AuthSignup {
    private UUID twinsUserId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private UUID publicKeyId;

    public enum Result {
        SIGNED_UP,
        EMAIL_VERIFICATION_REQUIRED,
        AWAITING_APPROVAL
    }
}
