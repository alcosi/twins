package org.twins.core.featurer.identityprovider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.cambium.common.util.StringUtils.snakeToCamel;

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

    public List<Cookie> getCookies() {
        return entrySet().stream()
                .filter(it -> it.getKey() != null)
                .filter(it -> it.getValue() != null)
                .map(entry -> {
                    String camelCaseKey = snakeToCamel(entry.getKey());
                    return new Cookie(camelCaseKey, entry.getValue());
                })
                .collect(Collectors.toList());
    }

    public HttpServletResponse addCookiesToResponse(HttpServletResponse response) {
        List<Cookie> cookies = getCookies();
        cookies.forEach(response::addCookie);
        return response;
    }
}
