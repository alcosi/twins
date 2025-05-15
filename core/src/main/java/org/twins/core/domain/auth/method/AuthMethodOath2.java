package org.twins.core.domain.auth.method;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AuthMethodOath2 extends AuthMethod {
    private String iconUrl;
    private String label;
    private String redirectUrl;
}
