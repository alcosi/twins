package org.twins.core.featurer.identityprovider.connector;

import com.alcosi.identity.exception.parser.IdentityParserException;
import com.alcosi.identity.exception.parser.api.*;
import com.alcosi.identity.exception.parser.ids.IdentityInvalidClientParserException;
import com.alcosi.identity.exception.parser.ids.IdentityInvalidCredentialsParserException;
import com.alcosi.identity.exception.parser.ids.IdentityInvalidRefreshTokenParserException;
import com.alcosi.identity.service.error.IdentityErrorParser;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamEncrypted;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.twins.core.domain.auth.AuthSignup;
import org.twins.core.domain.auth.EmailVerificationByTwins;
import org.twins.core.domain.auth.EmailVerificationHolder;
import org.twins.core.domain.auth.method.AuthMethod;
import org.twins.core.domain.auth.method.AuthMethodPassword;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.identityprovider.ClientLogoutData;
import org.twins.core.featurer.identityprovider.ClientSideAuthData;
import org.twins.core.featurer.identityprovider.TokenMetaData;
import org.twins.core.service.auth.AuthService;

import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.twins.core.exception.ErrorCodeTwins.*;

@Component
@Featurer(id = FeaturerTwins.ID_1903,
        name = "ALCOSI IDS",
        description = "")
@RequiredArgsConstructor
@Slf4j
public class IdentityProviderAlcosi extends IdentityProviderConnector {

    private final RestTemplate restTemplate = new RestTemplate();
    private final IdentityErrorParser.Implementation parser = new IdentityErrorParser.Implementation();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuthService authService;

    @FeaturerParam(name = "Identity server token base uri")
    public static final FeaturerParamString identityServerTokenBaseUri = new FeaturerParamString("identityServerTokenBaseUri");

    @FeaturerParam(name = "Identity server base uri")
    public static final FeaturerParamString identityServerBaseUri = new FeaturerParamString("identityServerBaseUri");

    @FeaturerParam(name = "Client id")
    public static final FeaturerParamString clientId = new FeaturerParamString("clientId");

    @FeaturerParam(name = "Service scope")
    public static final FeaturerParamString serviceScope = new FeaturerParamString("serviceScope");

    @FeaturerParam(name = "Client scope")
    public static final FeaturerParamString clientScope = new FeaturerParamString("clientScope");

    @FeaturerParam(name = "Client secret")
    public static final FeaturerParamEncrypted clientSecret = new FeaturerParamEncrypted("clientSecret");

    @FeaturerParam(name = "Client introspection id")
    public static final FeaturerParamString clientIntrospectionId = new FeaturerParamString("clientIntrospectionId");

    @FeaturerParam(name = "Client introspection secret")
    public static final FeaturerParamEncrypted clientIntrospectionSecret = new FeaturerParamEncrypted("clientIntrospectionSecret");

    @FeaturerParam(name = "Client introspection secret")
    public static final FeaturerParamString activeBusinessAccountClaimName = new FeaturerParamString("activeBusinessAccountClaimName");

    @Override
    protected ClientSideAuthData login(Properties properties, String username, String password, String fingerprint) throws ServiceException {

        String requestBody = new StringJoiner("&")
                .add("grant_type=password")
                .add("client_id=" + clientId.extract(properties))
                .add("scope=" + serviceScope.extract(properties))
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
                .add("scope=" + serviceScope.extract(properties))
                .add("client_secret=" + clientSecret.extract(properties))
                .add("refresh_token=" + refreshToken)
                .toString();
        return getAuthData(requestBody, properties);
    }

    @Override
    protected TokenMetaData resolveAuthTokenMetaData(Properties properties, String token) throws ServiceException {

        URI url = URI.create(identityServerTokenBaseUri.extract(properties) + "/introspect");
        String requestBody = "token=" + token;
        HttpHeaders httpHeaders = getHttpHeaders(clientIntrospectionId.extract(properties), clientIntrospectionSecret.extract(properties));
        RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, httpHeaders, POST, url);
        ResponseEntity<String> responseEntity = makeRequest(requestEntity, String.class);

        JsonNode claims = toJsonNode(responseEntity.getBody());
        if (responseEntity.getStatusCode().value() != OK.value() || !isActive(claims.get("active"))) {
            throw new ServiceException(IDP_PROVIDED_TOKEN_IS_NOT_ACTIVE);
        }

        JsonNode sub = claims.get("sub");
        JsonNode clientId = claims.get("client_id");

        UUID userId = sub != null ? UUID.fromString(sub.asText()) : UUID.fromString(clientId.asText());

        JsonNode claim = claims.get(activeBusinessAccountClaimName.extract(properties));
        UUID businessAccountId = null;
        if (claim != null) {
            String value = claim.asText();
            Map<UUID, UUID> domainIdToBusinessAccountIdMap = toObject(value, new TypeReference<>() {
            });
            UUID domainId = authService.getApiUser().getDomainId();
            businessAccountId = domainIdToBusinessAccountIdMap.get(domainId);
        }
        return new TokenMetaData()
                .setUserId(userId)
                .setBusinessAccountId(businessAccountId);
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

    @Override
    public EmailVerificationHolder signupByEmailInitiate(Properties properties, AuthSignup authSignup) throws ServiceException {

        String token = getToken(properties);
        try {
            URI url = URI.create(identityServerBaseUri.extract(properties) + "/user/register");
            HttpHeaders httpHeaders = getHttpHeaders(token);
            UserRegistrationRequest requestBody = new UserRegistrationRequest(authSignup.getTwinsUserId(), authSignup.getEmail(), authSignup.getPassword());
            RequestEntity<UserRegistrationRequest> requestEntity = new RequestEntity<>(requestBody, httpHeaders, POST, url);
            makeRequest(requestEntity, Void.class);
            String activationCode = getEmailActivationCode(authSignup.getEmail(), token, properties);
            return new EmailVerificationByTwins()
                    .setIdpUserActivateCode(activationCode);
        } finally {
            revokeTokenAsync(token, TokenType.ACCESS_TOKEN, properties);
        }
    }

    @Override
    public void signupByEmailActivate(Properties properties, UUID twinsUserId, String email, String idpUserActivateToken) throws ServiceException {

        String token = getToken(properties);
        try {
            URI url = URI.create(identityServerBaseUri.extract(properties) + "/user" + URLEncoder.encode(email, UTF_8) + "/activate");
            HttpHeaders httpHeaders = getHttpHeaders(token);
            UserActivationRequest requestBody = new UserActivationRequest(idpUserActivateToken);
            RequestEntity<UserActivationRequest> requestEntity = new RequestEntity<>(requestBody, httpHeaders, POST, url);
            makeRequest(requestEntity, Void.class);
        } finally {
            revokeTokenAsync(token, TokenType.ACCESS_TOKEN, properties);
        }
    }

    @Override
    public void switchActiveBusinessAccount(Properties properties, String authToken, UUID domainId, UUID businessAccountId) throws ServiceException {

        String token = getToken(properties);
        try {
            UUID userId = authService.getApiUser().getUserId();
            User user = getUser(userId, token, properties);
            String claimType = activeBusinessAccountClaimName.extract(properties);
            Claim claim = getClaim(claimType, user.claims);
            if (claim == null) {
                String value = toString(Map.of(domainId, businessAccountId));
                Claim newClaim = new Claim(claimType, value);
                addClaim(newClaim, token, userId, properties);
            } else {
                Map<UUID, UUID> domainIdToBusinessAccountIdMap = toObject(claim.value, new TypeReference<>() {
                });
                domainIdToBusinessAccountIdMap.put(domainId, businessAccountId);
                String value = toString(domainIdToBusinessAccountIdMap);
                Claim newClaim = new Claim(claimType, value);
                editClaim(claim, newClaim, token, userId, properties);
            }
        } finally {
            revokeTokenAsync(token, TokenType.ACCESS_TOKEN, properties);
        }
    }

    @Override
    protected ClientSideAuthData m2mAuth(Properties properties, String clientId, String clientSecret) throws ServiceException {

        URI url = URI.create(identityServerTokenBaseUri.extract(properties) + "/token");
        String requestBody = new StringJoiner("&")
                .add("grant_type=client_credentials")
                .add("client_id=" + clientId)
//                .add("scope=" + clientScope.extract(properties))
                .add("client_secret=" + clientSecret)
                .toString();
        HttpHeaders httpHeaders = getHttpHeaders(APPLICATION_FORM_URLENCODED);
        RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, httpHeaders, POST, url);
        ResponseEntity<TokenResponse> responseEntity = makeRequest(requestEntity, TokenResponse.class);
        var accessToken = responseEntity.getBody().accessToken;
        return new ClientSideAuthData().putAuthToken(accessToken);
    }

    private Claim getClaim(String claimType, List<Claim> claims) {
        if (isEmpty(claims)) return null;
        return claims.stream()
                .filter(claim -> Objects.equals(claimType, claim.type))
                .findFirst()
                .orElse(null);
    }

    private User getUser(UUID userId, String token, Properties properties) throws ServiceException {

        URI url = URI.create(identityServerBaseUri.extract(properties) + "/user/" + userId);
        HttpHeaders httpHeaders = getHttpHeaders(token);
        RequestEntity<Void> requestEntity = new RequestEntity<>(null, httpHeaders, GET, url);
        return makeRequest(requestEntity, User.class).getBody();
    }

    private record TokenResponse(@JsonProperty("access_token") String accessToken,
                                 @JsonProperty("refresh_token") String refreshToken) {
    }

    private record UserRegistrationRequest(@JsonProperty("id") UUID id,
                                           @JsonProperty("emailOrPhone") String emailOrPhone,
                                           @JsonProperty("password") String password) {
    }

    private record UserActivationRequest(@JsonProperty("activationCode") String activationToken) {
    }

    private record ActivationTokenResponse(@JsonProperty("token") String token) {
    }

    private record User(@JsonProperty("id") UUID id,
                        @JsonProperty("claims") List<Claim> claims) {
    }

    private record Claim(@JsonProperty("type") String type,
                         @JsonProperty("value") String value) {
    }

    private record ClaimUpdateRequest(@JsonProperty("oldClaim") Claim oldClaim,
                                      @JsonProperty("newClaim") Claim newClaim) {
    }

    private enum TokenType {
        ACCESS_TOKEN,
        REFRESH_TOKEN
    }

    private ClientSideAuthData getAuthData(String requestBody, Properties properties) throws ServiceException {

        URI url = URI.create(identityServerTokenBaseUri.extract(properties) + "/token");
        HttpHeaders httpHeaders = getHttpHeaders(APPLICATION_FORM_URLENCODED);
        RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, httpHeaders, POST, url);
        ResponseEntity<TokenResponse> responseEntity = makeRequest(requestEntity, TokenResponse.class);
        TokenResponse responseBody = responseEntity.getBody();

        ClientSideAuthData authData = new ClientSideAuthData();
        authData.putAuthToken(responseBody.accessToken);
        authData.putRefreshToken(responseBody.refreshToken);

        return authData;
    }

    private HttpHeaders getHttpHeaders(String token) {

        HttpHeaders httpHeaders = getHttpHeaders(APPLICATION_JSON);
        httpHeaders.setBearerAuth(token);
        httpHeaders.add("x-api-version", "2.0");
        return httpHeaders;
    }

    private HttpHeaders getHttpHeaders(String id, String secret) {

        HttpHeaders httpHeaders = getHttpHeaders(APPLICATION_FORM_URLENCODED);
        httpHeaders.setBasicAuth(id, secret);
        return httpHeaders;
    }

    private HttpHeaders getHttpHeaders(MediaType mediaType) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(mediaType);
        return httpHeaders;
    }

    private JsonNode toJsonNode(String content) throws ServiceException {
        try {
            return objectMapper.readTree(content);
        } catch (Exception exception) {
            log.error("Error processing JSON", exception);
            throw new ServiceException(IDP_INTERNAL_SERVER_ERROR);
        }
    }

    private String toString(Object value) throws ServiceException {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            log.error("Error processing JSON", exception);
            throw new ServiceException(IDP_INTERNAL_SERVER_ERROR);
        }
    }

    private <T> T toObject(String content, TypeReference<T> valueTypeRef) throws ServiceException {
        try {
            return objectMapper.readValue(content, valueTypeRef);
        } catch (Exception exception) {
            log.error("Error processing JSON", exception);
            throw new ServiceException(IDP_INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isActive(JsonNode claim) {
        if (claim == null) return false;
        return claim.asBoolean(false);
    }

    private void revokeToken(String token, TokenType tokenType, Properties properties) throws ServiceException {

        URI url = URI.create(identityServerTokenBaseUri.extract(properties) + "/revocation");
        String requestBody = new StringJoiner("&")
                .add("token=" + token)
                .add("token_type_hint=" + tokenType.name().toLowerCase())
                .toString();
        HttpHeaders httpHeaders = getHttpHeaders(clientId.extract(properties), clientSecret.extract(properties));
        RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, httpHeaders, POST, url);
        makeRequest(requestEntity, Void.class);
    }

    private void revokeTokenAsync(String token, TokenType tokenType, Properties properties) {

        CompletableFuture.runAsync(() -> {
            try {
                revokeToken(token, tokenType, properties);
            } catch (Exception exception) {
                log.error("Error while revoking token");
            }
        });
    }

    private <T> ResponseEntity<T> makeRequest(RequestEntity<?> requestEntity, Class<T> responseType) throws ServiceException {
        try {
            return restTemplate.exchange(requestEntity, responseType);
        } catch (HttpClientErrorException.Unauthorized exception) {
            log.error("Unauthorized request");
            throw new ServiceException(IDP_AUTHENTICATION_EXCEPTION);
        } catch (HttpClientErrorException exception) {
            String responseBody = exception.getResponseBodyAsString();
            log.error(responseBody);
            resolveError(responseBody, exception.getStatusCode().value());
        } catch (RestClientException exception) {
            throw new ServiceException(IDP_INTERNAL_SERVER_ERROR);
        }
        throw new ServiceException(IDP_INTERNAL_SERVER_ERROR);
    }

    private void resolveError(String responseBody, int responseStatus) throws ServiceException {
        try {
            parser.processAnyException(responseStatus, () -> responseBody);
        } catch (IdentityInvalidClientParserException exception) {
            throw new ServiceException(IDP_INVALID_CLIENT_CREDENTIALS);
        } catch (IdentityInvalidCredentialsParserException exception) {
            throw new ServiceException(IDP_UNAUTHORIZED);
        } catch (IdentityInvalidRefreshTokenParserException exception) {
            throw new ServiceException(IDP_INCORRECT_REFRESH_TOKEN);
        } catch (IdentityPasswordIsNotStrongEnoughParserException exception) {
            throw new ServiceException(IDP_REGISTRATION_INCORRECT_PASSWORD_FORMAT);
        } catch (IdentityProfileIsAlreadyExistsParserException exception) {
            throw new ServiceException(IDP_SIGNUP_EMAIL_ALREADY_REGISTERED);
        } catch (IdentityProfileIsIncorrectRequestParserException exception) {
            throw new ServiceException(IDP_INVALID_INPUT_DATA);
        } catch (IdentityProfileIsAlreadyActivatedParserException exception) {
            throw new ServiceException(IDP_ACCOUNT_ALREADY_ACTIVATED);
        } catch (IdentityInvalidActivationCodeParserException exception) {
            throw new ServiceException(IDP_ACCOUNT_ACTIVATION_FAILED);
        } catch (IdentityProfileNotExistOnIdentityParserException exception) {
            throw new ServiceException(IDP_USER_NOT_FOUND);
        } catch (IdentityParserException exception) {
            throw new ServiceException(IDP_BAD_REQUEST);
        }
    }

    private String getEmailActivationCode(String email, String token, Properties properties) throws ServiceException {

        URI url = URI.create(identityServerBaseUri.extract(properties) + "/user/" + URLEncoder.encode(email, UTF_8) + "/activate");
        HttpHeaders httpHeaders = getHttpHeaders(token);
        RequestEntity<Void> requestEntity = new RequestEntity<>(null, httpHeaders, GET, url);
        return makeRequest(requestEntity, ActivationTokenResponse.class).getBody().token;
    }

    private String getToken(Properties properties) throws ServiceException {

        URI url = URI.create(identityServerTokenBaseUri.extract(properties) + "/token");
        String requestBody = new StringJoiner("&")
                .add("grant_type=client_credentials")
                .add("client_id=" + clientId.extract(properties))
                .add("scope=" + clientScope.extract(properties))
                .add("client_secret=" + clientSecret.extract(properties))
                .toString();
        HttpHeaders httpHeaders = getHttpHeaders(APPLICATION_FORM_URLENCODED);
        RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, httpHeaders, POST, url);
        ResponseEntity<TokenResponse> responseEntity = makeRequest(requestEntity, TokenResponse.class);
        return responseEntity.getBody().accessToken;
    }

    private void addClaim(Claim claim, String token, UUID userId, Properties properties) throws ServiceException {

        URI url = URI.create(identityServerBaseUri.extract(properties) + "/user/" + userId + "/claim");
        HttpHeaders httpHeaders = getHttpHeaders(token);
        RequestEntity<Claim> requestEntity = new RequestEntity<>(claim, httpHeaders, POST, url);
        makeRequest(requestEntity, Void.class);
    }

    private void editClaim(Claim oldClaim, Claim newclaim, String token, UUID userId, Properties properties) throws ServiceException {

        URI url = URI.create(identityServerBaseUri.extract(properties) + "/user/" + userId + "/claim");
        HttpHeaders httpHeaders = getHttpHeaders(token);
        ClaimUpdateRequest requestBody = new ClaimUpdateRequest(oldClaim, newclaim);
        RequestEntity<ClaimUpdateRequest> requestEntity = new RequestEntity<>(requestBody, httpHeaders, PUT, url);
        makeRequest(requestEntity, Void.class);
    }
}
