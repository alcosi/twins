package org.twins.core.domain.auth;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AuthSignup {
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    public enum Result {
        SIGNED_UP,
        EMAIL_VERIFICATION_REQUIRED,
        AWAITING_APPROVAL
    }
}
