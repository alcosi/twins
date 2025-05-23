package org.twins.core.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CryptUtils;
import org.cambium.common.util.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.idp.IdentityProviderInternalTokenEntity;
import org.twins.core.dao.idp.IdentityProviderInternalTokenRepository;
import org.twins.core.dao.idp.IdentityProviderInternalUserEntity;
import org.twins.core.dao.idp.IdentityProviderInternalUserRepository;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.identityprovider.ClientSideAuthData;
import org.twins.core.featurer.identityprovider.TokenMetaData;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class IdentityProviderInternalService {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();
    private final IdentityProviderInternalUserRepository identityProviderInternalUserRepository;
    private final IdentityProviderInternalTokenRepository identityProviderInternalTokenRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthService authService;

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
        }
        ClientSideAuthData clientSideAuthData = new ClientSideAuthData()
                .putRefreshToken(generateToken(64))
                .putAuthToken(generateToken(32));
        IdentityProviderInternalTokenEntity token = new IdentityProviderInternalTokenEntity()
                .setDomainId(authService.getApiUser().getDomainId())
                .setUserId(user.getUserId())
                .setAccessToken(getTokenHash(clientSideAuthData.getAuthToken()))
                .setAccessExpiresAt(Timestamp.from(Instant.now().plusSeconds(authTokenExpiresInSeconds)))
                .setRefreshToken(getTokenHash(clientSideAuthData.getRefreshToken()))
                .setRefreshExpiresAt(Timestamp.from(Instant.now().plusSeconds(refreshTokenExpiresInSeconds)))
                .setFingerPrint(fingerprint)
                .setRevoked(false);
        identityProviderInternalTokenRepository.save(token);
        user.setLastLoginAt(Timestamp.from(Instant.now()));
        identityProviderInternalUserRepository.save(user);
        return clientSideAuthData;
    }

    public ClientSideAuthData refresh(String refreshToken, String fingerprint, long authTokenExpiresMills, long refreshTokenExpiresMills) throws ServiceException {
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
        ClientSideAuthData clientSideAuthData = new ClientSideAuthData()
                .putRefreshToken(generateToken(64))
                .putAuthToken(generateToken(32));
        token
                .setAccessToken(getTokenHash(clientSideAuthData.getAuthToken()))
                .setAccessExpiresAt(Timestamp.from(Instant.now().plusMillis(authTokenExpiresMills)))
                .setRefreshToken(getTokenHash(clientSideAuthData.getRefreshToken()))
                .setRefreshExpiresAt(Timestamp.from(Instant.now().plusMillis(refreshTokenExpiresMills)));
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
}
