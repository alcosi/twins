package org.twins.core.domain.auth;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AuthLogin {
    private String username;
    private String password;
}
