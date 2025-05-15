package org.twins.core.domain.auth.method;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AuthMethodPassword extends AuthMethod {
    private boolean registerSupported;
    private boolean recoverSupported;
}
