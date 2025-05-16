package org.twins.core.featurer.identityprovider;

import lombok.Data;

@Data
public class ClientTokenData {
    private String authToken;
    private String refreshToken;
}
