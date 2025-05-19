package org.twins.core.domain.auth.method;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AuthMethodOath2 extends AuthMethod {
    private String iconUrl;
    private String label;
    private String redirectUrl;
}
