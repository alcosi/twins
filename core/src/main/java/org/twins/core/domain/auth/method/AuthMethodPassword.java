package org.twins.core.domain.auth.method;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AuthMethodPassword extends AuthMethod {
    private boolean registerSupported;
    private boolean recoverSupported;
    private boolean fingerprintRequired;
}
