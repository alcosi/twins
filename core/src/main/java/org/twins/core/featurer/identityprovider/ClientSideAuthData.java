package org.twins.core.featurer.identityprovider;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClientSideAuthData extends HashMap<String, String> {
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String REFRESH_TOKEN_EXPIRES_AT = "refresh_token_expires_at";
    public static final String AUTH_TOKEN = "auth_token";
    public static final String AUTH_TOKEN_EXPIRES_AT = "auth_token_expires_at";

    public ClientSideAuthData putRefreshToken(String refreshToken) {
        put(REFRESH_TOKEN, refreshToken);
        return this;
    }

    public ClientSideAuthData putRefreshTokenExpiresAt(String refreshTokenExpiresAt) {
        put(REFRESH_TOKEN_EXPIRES_AT, refreshTokenExpiresAt);
        return this;
    }

    public ClientSideAuthData putAuthToken(String refreshToken) {
        put(AUTH_TOKEN, refreshToken);
        return this;
    }

    public ClientSideAuthData putAuthTokenExpiresAt(String authTokenExpiresAt) {
        put(AUTH_TOKEN_EXPIRES_AT, authTokenExpiresAt);
        return this;
    }

    public String getRefreshToken() {
        return get(REFRESH_TOKEN);
    }

    public String getAuthToken() {
        return get(AUTH_TOKEN);
    }
}
