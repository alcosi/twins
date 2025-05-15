package org.twins.core.featurer.identityprovider.token;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClientTokenDataAuth extends ClientTokenData {
    private String authToken;
}
