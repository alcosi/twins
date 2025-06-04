package org.twins.core.featurer.identityprovider;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClientSideAuthData extends HashMap<String, String> {
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String AUTH_TOKEN = "auth_token";

    public ClientSideAuthData putRefreshToken(String refreshToken) {
        put(REFRESH_TOKEN, refreshToken);
        return this;
    }

    public ClientSideAuthData putAuthToken(String refreshToken) {
        put(AUTH_TOKEN, refreshToken);
        return this;
    }

    public String getRefreshToken() {
        return get(REFRESH_TOKEN);
    }

    public String getAuthToken() {
        return get(AUTH_TOKEN);
    }
}
