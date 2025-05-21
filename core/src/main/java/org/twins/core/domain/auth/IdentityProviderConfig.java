package org.twins.core.domain.auth;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.idp.IdentityProviderEntity;
import org.twins.core.domain.auth.method.AuthMethod;

import java.util.List;

@Data
@Accessors(chain = true)
public class IdentityProviderConfig {
    IdentityProviderEntity identityProvider;
    List<AuthMethod> supportedMethods;
}
