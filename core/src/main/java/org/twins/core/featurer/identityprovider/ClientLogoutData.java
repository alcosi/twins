package org.twins.core.featurer.identityprovider;

import lombok.Data;

@Data
public class ClientLogoutData {
    private String authToken;
    private String refreshToken;
}
