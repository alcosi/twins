package org.twins.core.featurer.identityprovider;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.domain.auth.CryptKey;

@Data
@Accessors (chain = true)
public class M2MAuthData {
    private ClientSideAuthData clientSideAuthData;
    private CryptKey.CryptPublicKey actAsUserKey;
}
