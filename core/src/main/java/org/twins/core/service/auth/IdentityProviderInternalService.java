package org.twins.core.service.auth;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CryptUtils;
import org.cambium.common.util.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.idp.IdentityProviderInternalTokenEntity;
import org.twins.core.dao.idp.IdentityProviderInternalTokenRepository;
import org.twins.core.dao.idp.IdentityProviderInternalUserEntity;
import org.twins.core.dao.idp.IdentityProviderInternalUserRepository;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.auth.AuthSignup;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.identityprovider.ClientSideAuthData;
import org.twins.core.featurer.identityprovider.TokenMetaData;
import org.twins.core.service.HttpRequestService;
import org.twins.core.service.domain.DomainUserService;
import org.twins.core.service.user.UserService;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class IdentityProviderInternalService {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();
    private final IdentityProviderInternalUserRepository identityProviderInternalUserRepository;
    private final IdentityProviderInternalTokenRepository identityProviderInternalTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthService authService;
    private final UserService userService;
    private final HttpRequestService httpRequestService;
    private final DomainUserService domainUserService;

    public static String generateToken(int byteLength) {
        byte[] randomBytes = new byte[byteLength];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public ClientSideAuthData login(String username, String password, String fingerprint, long authTokenExpiresInSeconds, long refreshTokenExpiresInSeconds) throws ServiceException {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new ServiceException(ErrorCodeTwins.IDP_EMPTY_USERNAME_OR_PASSWORD);
        }
        IdentityProviderInternalUserEntity user = identityProviderInternalUserRepository.findByUser_Email(username);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ServiceException(ErrorCodeTwins.IDP_UNAUTHORIZED);
        } else if (!user.isActive()) {
            throw new ServiceException(ErrorCodeTwins.IDP_USER_IS_INACTIVE);
        }
        Instant authTokenExpiresAt = Instant.now().plusSeconds(authTokenExpiresInSeconds);
        Instant refreshTokenExpiresAt = Instant.now().plusSeconds(refreshTokenExpiresInSeconds);
        ClientSideAuthData clientSideAuthData = new ClientSideAuthData()
                .putRefreshToken(generateToken(64))
                .putAuthToken(generateToken(32))
                .putAuthTokenExpiresAt(authTokenExpiresAt.toString())
                .putRefreshTokenExpiresAt(refreshTokenExpiresAt.toString());
        IdentityProviderInternalTokenEntity token = new IdentityProviderInternalTokenEntity()
                .setDomainId(authService.getApiUser().getDomainId())
                .setUserId(user.getUserId())
                .setAccessToken(getTokenHash(clientSideAuthData.getAuthToken()))
                .setAccessExpiresAt(Timestamp.from(authTokenExpiresAt))
                .setRefreshToken(getTokenHash(clientSideAuthData.getRefreshToken()))
                .setRefreshExpiresAt(Timestamp.from(refreshTokenExpiresAt))
                .setFingerPrint(fingerprint)
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setRevoked(false);
        identityProviderInternalTokenRepository.save(token);
        user.setLastLoginAt(Timestamp.from(Instant.now()));
        identityProviderInternalUserRepository.save(user);
        return clientSideAuthData;
    }

    public ClientSideAuthData m2mToken(String clientId, String clientSecret, long authTokenExpiresInSeconds) throws ServiceException {
        if (StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret)) {
            throw new ServiceException(ErrorCodeTwins.IDP_EMPTY_CLIENT_ID_OR_SECRET);
        }
        IdentityProviderInternalUserEntity user = identityProviderInternalUserRepository.findByUserId(UUID.fromString(clientId));
        if (user == null || !passwordEncoder.matches(clientSecret, user.getPasswordHash())) {
            throw new ServiceException(ErrorCodeTwins.IDP_UNAUTHORIZED);
        } else if (!user.isActive()) {
            throw new ServiceException(ErrorCodeTwins.IDP_USER_IS_INACTIVE);
        }
        Instant authTokenExpiresAt = Instant.now().plusSeconds(authTokenExpiresInSeconds);
        ClientSideAuthData clientSideAuthData = new ClientSideAuthData()
                .putAuthToken(generateToken(32))
                .putAuthTokenExpiresAt(authTokenExpiresAt.toString());
        IdentityProviderInternalTokenEntity token = new IdentityProviderInternalTokenEntity()
                .setDomainId(authService.getApiUser().getDomainId())
                .setUserId(user.getUserId())
                .setAccessToken(getTokenHash(clientSideAuthData.getAuthToken()))
                .setAccessExpiresAt(Timestamp.from(authTokenExpiresAt))
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setRevoked(false);
        identityProviderInternalTokenRepository.save(token);
        user.setLastLoginAt(Timestamp.from(Instant.now()));
        identityProviderInternalUserRepository.save(user);
        return clientSideAuthData;
    }

    public ClientSideAuthData refresh(String refreshToken, String fingerprint, long authTokenExpiresSec, long refreshTokenExpiresSec) throws ServiceException {
        if (StringUtils.isEmpty(refreshToken)) {
            throw new ServiceException(ErrorCodeTwins.IDP_INCORRECT_REFRESH_TOKEN);
        }
        IdentityProviderInternalTokenEntity token;
        if (StringUtils.isEmpty(fingerprint)) {
            token = identityProviderInternalTokenRepository.findByRefreshToken(getTokenHash(refreshToken));
        } else {
            token = identityProviderInternalTokenRepository.findByRefreshTokenAndFingerPrint(getTokenHash(refreshToken), fingerprint);
        }
        if (token == null) {
            throw new ServiceException(ErrorCodeTwins.IDP_INCORRECT_REFRESH_TOKEN);
        }
        Instant authTokenExpiresAt = Instant.now().plusSeconds(authTokenExpiresSec);
        Instant refreshTokenExpiresAt = Instant.now().plusSeconds(refreshTokenExpiresSec);
        ClientSideAuthData clientSideAuthData = new ClientSideAuthData()
                .putRefreshToken(generateToken(64))
                .putAuthToken(generateToken(32))
                .putAuthTokenExpiresAt(authTokenExpiresAt.toString())
                .putRefreshTokenExpiresAt(refreshTokenExpiresAt.toString());
        token
                .setAccessToken(getTokenHash(clientSideAuthData.getAuthToken()))
                .setAccessExpiresAt(Timestamp.from(authTokenExpiresAt))
                .setRefreshToken(getTokenHash(clientSideAuthData.getRefreshToken()))
                .setRefreshExpiresAt(Timestamp.from(refreshTokenExpiresAt));
        identityProviderInternalTokenRepository.save(token);
        return clientSideAuthData;
    }

    public TokenMetaData resolve(String authToken) throws ServiceException {
        if (StringUtils.isEmpty(authToken)) {
            throw new ServiceException(ErrorCodeTwins.IDP_INCORRECT_AUTH_TOKEN);
        }
        IdentityProviderInternalTokenEntity token = identityProviderInternalTokenRepository.findByAccessToken(getTokenHash(authToken));
        if (token == null) {
            throw new ServiceException(ErrorCodeTwins.IDP_INCORRECT_AUTH_TOKEN);
        }
        if (token.getAccessExpiresAt() == null
                || token.getAccessExpiresAt().before(Timestamp.from(Instant.now()))
                || token.isRevoked()) {
            throw new ServiceException(ErrorCodeTwins.IDP_INCORRECT_AUTH_TOKEN);
        }
        return new TokenMetaData()
                .setUserId(token.getUserId())
                .setBusinessAccountId(token.getActiveBusinessAccountId());
    }

    public static String getTokenHash(String token) {
        return CryptUtils.sha256(token);
    }

    public void signupByEmailInitiate(AuthSignup authSignup) throws ServiceException {
        IdentityProviderInternalUserEntity internalUserEntity = identityProviderInternalUserRepository.findByUser_Email(authSignup.getEmail());
        if (internalUserEntity != null) {
            throw new ServiceException(ErrorCodeTwins.IDP_SIGNUP_EMAIL_ALREADY_REGISTERED);
        }
        internalUserEntity = new IdentityProviderInternalUserEntity()
                .setUserId(authSignup.getTwinsUserId())
                .setLastLoginAt(Timestamp.from(Instant.now()))
                .setPasswordHash(passwordEncoder.encode(authSignup.getPassword()))
                .setActive(false)
                .setCreatedAt(Timestamp.from(Instant.now()));
        identityProviderInternalUserRepository.save(internalUserEntity);
    }

    public void signupByEmailActivate(UUID twinsUserId) throws ServiceException {
        IdentityProviderInternalUserEntity internalUserEntity = identityProviderInternalUserRepository.findByUserId(twinsUserId);
        if (internalUserEntity == null) {
            throw new ServiceException(ErrorCodeTwins.IDP_UNAUTHORIZED); //todo change code
        }
        internalUserEntity.setActive(true);
        identityProviderInternalUserRepository.save(internalUserEntity);
    }

    public void switchActiveBusinessAccount(String authToken, UUID domainId, UUID businessAccountId) throws ServiceException {
        IdentityProviderInternalTokenEntity token = identityProviderInternalTokenRepository.findByAccessToken(getTokenHash(authToken));
        if (token == null) {
            throw new ServiceException(ErrorCodeTwins.IDP_INCORRECT_AUTH_TOKEN);
        }
        if (!businessAccountId.equals(token.getActiveBusinessAccountId())) {
            token.setActiveBusinessAccountId(businessAccountId);
            identityProviderInternalTokenRepository.save(token);
        }
        DomainUserEntity domainUserEntity = domainUserService.getDomainUserV2();
        if (!businessAccountId.equals(domainUserEntity.getLastActiveBusinessAccountId())) {
            domainUserEntity.setLastActiveBusinessAccountId(businessAccountId);
            domainUserService.saveSafe(domainUserEntity);
        }
    }
}
