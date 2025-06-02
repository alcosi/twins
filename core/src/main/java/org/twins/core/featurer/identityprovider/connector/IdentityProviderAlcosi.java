package org.twins.core.featurer.identityprovider.connector;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamEncrypted;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.twins.core.domain.auth.AuthSignup;
import org.twins.core.domain.auth.EmailVerificationMode;
import org.twins.core.domain.auth.method.AuthMethod;
import org.twins.core.domain.auth.method.AuthMethodPassword;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.identityprovider.ClientLogoutData;
import org.twins.core.featurer.identityprovider.ClientSideAuthData;
import org.twins.core.featurer.identityprovider.TokenMetaData;

import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.twins.core.exception.ErrorCodeTwins.*;

@Component
@Featurer(id = FeaturerTwins.ID_1903,
        name = "ALCOSI IDS",
        description = "")
@RequiredArgsConstructor
public class IdentityProviderAlcosi extends IdentityProviderConnector {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @FeaturerParam(name = "Identity base uri")
    public static final FeaturerParamString identityServerBaseUri = new FeaturerParamString("identityServerBaseUri");

    @FeaturerParam(name = "Client id")
    public static final FeaturerParamEncrypted clientId = new FeaturerParamEncrypted("clientId");

    @FeaturerParam(name = "Scope")
    public static final FeaturerParamString scope = new FeaturerParamString("scope");

    @FeaturerParam(name = "Client secret")
    public static final FeaturerParamEncrypted clientSecret = new FeaturerParamEncrypted("clientSecret");

    @FeaturerParam(name = "Client introspection id")
    public static final FeaturerParamEncrypted clientIntrospectionId = new FeaturerParamEncrypted("clientIntrospectionId");

    @FeaturerParam(name = "Client introspection secret")
    public static final FeaturerParamEncrypted clientIntrospectionSecret = new FeaturerParamEncrypted("clientIntrospectionSecret");

    @FeaturerParam(name = "Client introspection secret")
    public static final FeaturerParamString activeBusinessAccountClaimName = new FeaturerParamString("activeBusinessAccountClaimName");

    @Override
    protected ClientSideAuthData login(Properties properties, String username, String password, String fingerprint) throws ServiceException {

        String requestBody = new StringJoiner("&")
                .add("grant_type=password")
                .add("client_id=" + clientId.extract(properties))
                .add("scope=" + scope.extract(properties))
                .add("client_secret=" + clientSecret.extract(properties))
                .add("username=" + URLEncoder.encode(username, UTF_8))
                .add("password=" + URLEncoder.encode(password, UTF_8))
                .toString();
        return getAuthData(requestBody, properties);
    }

    @Override
    protected ClientSideAuthData refresh(Properties properties, String refreshToken, String fingerprint) throws ServiceException {

        String requestBody = new StringJoiner("&")
                .add("grant_type=refresh_token")
                .add("client_id=" + clientId.extract(properties))
                .add("scope=" + scope.extract(properties))
                .add("client_secret=" + clientSecret.extract(properties))
                .add("refresh_token=" + refreshToken)
                .toString();
        return getAuthData(requestBody, properties);
    }

    @Override
    protected TokenMetaData resolveAuthTokenMetaData(Properties properties, String token) throws ServiceException {

        URI url = URI.create(identityServerBaseUri.extract(properties) + "/introspect");
        String requestBody = "token=" + token;
        HttpHeaders httpHeaders = getHttpHeaders();
        httpHeaders.setBasicAuth(clientIntrospectionId.extract(properties), clientIntrospectionSecret.extract(properties));
        RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, httpHeaders, POST, url);
        ResponseEntity<String> responseEntity = makeRequest(requestEntity, String.class);

        JsonNode claims = getClaims(responseEntity.getBody());
        if (responseEntity.getStatusCode().value() != OK.value() || !isActive(claims.get("active"))) {
            throw new ServiceException(IDP_PROVIDED_TOKEN_IS_NOT_ACTIVE);
        }
        try {
            UUID userId = UUID.fromString(claims.get("sub").asText());
            String property = activeBusinessAccountClaimName.extract(properties);
            UUID businessAccountId = property == null ? null : UUID.fromString(claims.get(property).asText());
            return new TokenMetaData()
                    .setUserId(userId)
                    .setBusinessAccountId(businessAccountId);
        } catch (Exception exception) {
            throw new ServiceException(IDP_PROVIDED_TOKEN_IS_NOT_ACTIVE);
        }
    }

    @Override
    public List<AuthMethod> getSupportedMethods(Properties properties) {
        return List.of(new AuthMethodPassword()
                .setRegisterSupported(false)
                .setRecoverSupported(false)
                .setFingerprintRequired(false));
    }

    @Override
    public void logout(Properties properties, ClientLogoutData clientLogoutData) throws ServiceException {

        String accessToken = clientLogoutData.get("accessToken");
        String refreshToken = clientLogoutData.get("refreshToken");
        revokeToken(accessToken, TokenType.ACCESS_TOKEN, properties);
        revokeToken(refreshToken, TokenType.REFRESH_TOKEN, properties);
    }

    private record TokenResponse(@JsonProperty("access_token") String accessToken,
                                 @JsonProperty("refresh_token") String refreshToken) {
    }

    private record ErrorResponse(@JsonProperty("error") String error,
                                 @JsonProperty("error_description") String errorDescription) {
    }

    private enum TokenType {
        ACCESS_TOKEN,
        REFRESH_TOKEN
    }

    private ClientSideAuthData getAuthData(String requestBody, Properties properties) throws ServiceException {

        URI url = URI.create(identityServerBaseUri.extract(properties) + "/token");
        HttpHeaders httpHeaders = getHttpHeaders();
        RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, httpHeaders, POST, url);
        ResponseEntity<TokenResponse> responseEntity = makeRequest(requestEntity, TokenResponse.class);
        TokenResponse responseBody = responseEntity.getBody();

        ClientSideAuthData authData = new ClientSideAuthData();
        authData.put("accessToken", responseBody.accessToken);
        authData.put("refreshToken", responseBody.refreshToken);

        return authData;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(APPLICATION_FORM_URLENCODED);
        return httpHeaders;
    }

    private JsonNode getClaims(String content) throws ServiceException {
        try {
            return objectMapper.readTree(content);
        } catch (Exception exception) {
            throw new ServiceException(IDP_AUTHENTICATION_EXCEPTION);
        }
    }

    private boolean isActive(JsonNode claim) {
        if (claim == null) return false;
        return claim.asBoolean(false);
    }

    private void revokeToken(String token, TokenType tokenType, Properties properties) throws ServiceException {

        URI url = URI.create(identityServerBaseUri.extract(properties) + "/revocation");
        String requestBody = new StringJoiner("&")
                .add("token=" + token)
                .add("token_type_hint=" + tokenType.name().toLowerCase())
                .toString();
        HttpHeaders httpHeaders = getHttpHeaders();
        httpHeaders.setBasicAuth(clientId.extract(properties), clientSecret.extract(properties));
        RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, httpHeaders, POST, url);
        makeRequest(requestEntity, Void.class);
    }

    private <T> ResponseEntity<T> makeRequest(RequestEntity<?> requestEntity, Class<T> responseType) throws ServiceException {
        try {
            return restTemplate.exchange(requestEntity, responseType);
        } catch (HttpClientErrorException exception) {
            ErrorResponse errorResponse = getErrorResponse(exception.getResponseBodyAsByteArray());
            resolveError(errorResponse);
        } catch (Exception exception) {
            throw new ServiceException(IDP_AUTHENTICATION_EXCEPTION);
        }
        throw new ServiceException(IDP_AUTHENTICATION_EXCEPTION);
    }

    private void resolveError(ErrorResponse errorResponse) throws ServiceException {
        String error = errorResponse.error;
        String errorDescription = errorResponse.errorDescription;
        if ("invalid_grant".equals(error) && "Invalid username or password".equals(errorDescription)) {
            throw new ServiceException(IDP_UNAUTHORIZED);
        } else if ("invalid_grant".equals(error) && errorDescription == null) {
            throw new ServiceException(IDP_INCORRECT_REFRESH_TOKEN);
        } else {
            throw new ServiceException(IDP_AUTHENTICATION_EXCEPTION);
        }
    }

    private ErrorResponse getErrorResponse(byte[] responseBody) throws ServiceException {
        try {
            return objectMapper.readValue(responseBody, ErrorResponse.class);
        } catch (Exception exception) {
            throw new ServiceException(IDP_AUTHENTICATION_EXCEPTION);
        }
    }

    @Override
    public EmailVerificationMode signupByEmailInitiate(Properties properties, AuthSignup authSignup) throws ServiceException {
        throw new ServiceException(IDP_SIGNUP_NOT_SUPPORTED);
    }

    @Override
    public void signupByEmailActivate(Properties properties, UUID twinsUserId, String email, String idpUserActivateToken) throws ServiceException {
        throw new ServiceException(IDP_SIGNUP_NOT_SUPPORTED);
    }
}
