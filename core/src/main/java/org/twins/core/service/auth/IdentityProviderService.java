package org.twins.core.service.auth;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CryptUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySmartService;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.idp.IdentityProviderEntity;
import org.twins.core.dao.idp.IdentityProviderRepository;
import org.twins.core.dao.user.UserEmailVerificationEntity;
import org.twins.core.dao.user.UserEmailVerificationRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.ActAsUser;
import org.twins.core.domain.apiuser.BusinessAccountResolverGivenId;
import org.twins.core.domain.apiuser.UserResolverGivenId;
import org.twins.core.domain.auth.*;
import org.twins.core.enums.auth.EmailVerificationType;
import org.twins.core.enums.domain.DomainType;
import org.twins.core.enums.user.UserStatus;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.identityprovider.ClientLogoutData;
import org.twins.core.featurer.identityprovider.ClientSideAuthData;
import org.twins.core.featurer.identityprovider.M2MAuthData;
import org.twins.core.featurer.identityprovider.TokenMetaData;
import org.twins.core.featurer.identityprovider.connector.IdentityProviderConnector;
import org.twins.core.featurer.identityprovider.trustor.Trustor;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.domain.DomainUserService;
import org.twins.core.service.event.Event;
import org.twins.core.service.event.Events;
import org.twins.core.service.notification.NotificationEmailService;
import org.twins.core.service.user.UserService;

import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class IdentityProviderService extends TwinsEntitySecureFindService<IdentityProviderEntity> {
    private final AuthService authService;
    private final FeaturerService featurerService;
    private final IdentityProviderRepository identityProviderRepository;
    private final UserEmailVerificationRepository userEmailVerificationRepository;
    private final UserService userService;
    private final ApiUserResolverService apiUserResolverService;
    @Lazy
    private final DomainUserService domainUserService;
    private final NotificationEmailService notificationEmailService;

    @Override
    public CrudRepository<IdentityProviderEntity, UUID> entityRepository() {
        return identityProviderRepository;
    }

    @Override
    public Function<IdentityProviderEntity, UUID> entityGetIdFunction() {
        return IdentityProviderEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(IdentityProviderEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(IdentityProviderEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @NotNull
    private IdentityProviderEntity getDomainIdentityProviderSafe() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        IdentityProviderEntity identityProvider = apiUser.getDomain().getIdentityProvider();
        return checkIdentityProviderActive(identityProvider);
    }

    private static IdentityProviderEntity checkIdentityProviderActive(IdentityProviderEntity identityProvider) throws ServiceException {
        if (identityProvider.getStatus() != IdentityProviderEntity.IdentityProviderStatus.ACTIVE) {
            throw new ServiceException(ErrorCodeTwins.IDP_IS_NOT_ACTIVE);
        }
        return identityProvider;
    }

    public ClientSideAuthData login(AuthLogin authLogin) throws ServiceException {
        IdentityProviderEntity identityProvider = getDomainIdentityProviderSafe();
        if (authLogin.getPublicKeyId() != null) {
            authLogin.setPassword(decryptPassword(authLogin.getPassword(), authLogin.getPublicKeyId()));
        }
        IdentityProviderConnector identityProviderConnector = featurerService.getFeaturer(identityProvider.getIdentityProviderConnectorFeaturerId(), IdentityProviderConnector.class);
        ClientSideAuthData clientSideAuthData = identityProviderConnector.login(identityProvider.getIdentityProviderConnectorParams(), authLogin.getUsername(), authLogin.getPassword(), authLogin.getFingerPrint());
        TokenMetaData tokenMetaData = resolveAuthTokenMetaData(clientSideAuthData.getAuthToken());
        authService.getApiUser()
                .setUserResolver(new UserResolverGivenId(tokenMetaData.getUserId()))
                .setBusinessAccountResolver(new BusinessAccountResolverGivenId(tokenMetaData.getBusinessAccountId()));
        if (authService.getApiUser().getDomain().getDomainType() == DomainType.b2b && tokenMetaData.getBusinessAccountId() == null) {
            //looks like we need to switch active BA

            DomainUserEntity domainUserEntity = domainUserService.findByUserId(tokenMetaData.getUserId());
            if (domainUserEntity.getLastActiveBusinessAccountId() != null) {
                switchActiveBusinessAccount(clientSideAuthData.getAuthToken(), tokenMetaData.getUserId(), domainUserEntity.getLastActiveBusinessAccountId());
            }
        }
        return clientSideAuthData;
    }

    public M2MAuthData m2mAuth(AuthM2MGetToken m2mLogin) throws ServiceException {
        IdentityProviderEntity identityProvider = getDomainIdentityProviderSafe();
        if (m2mLogin.getPublicKeyId() != null) {
            m2mLogin.setClientSecret(decryptPassword(m2mLogin.getClientSecret(), m2mLogin.getPublicKeyId()));
        }
        IdentityProviderConnector identityProviderConnector = featurerService.getFeaturer(identityProvider.getIdentityProviderConnectorFeaturerId(), IdentityProviderConnector.class);
        Trustor trustor = featurerService.getFeaturer(identityProvider.getTrustorFeaturerId(), Trustor.class);
        ClientSideAuthData clientSideAuthData = identityProviderConnector.m2mAuth(identityProvider.getIdentityProviderConnectorParams(), m2mLogin.getClientId(), m2mLogin.getClientSecret());
        M2MAuthData m2MAuthData = new M2MAuthData()
                .setClientSideAuthData(clientSideAuthData)
                .setActAsUserKey(trustor.getActAsUserPublicKey(identityProvider.getTrustorParams()));
        return m2MAuthData;
    }

    public void logout(ClientLogoutData logoutData) throws ServiceException {
        IdentityProviderEntity identityProvider = getDomainIdentityProviderSafe();
        IdentityProviderConnector identityProviderConnector = featurerService.getFeaturer(identityProvider.getIdentityProviderConnectorFeaturerId(), IdentityProviderConnector.class);
        identityProviderConnector.logout(identityProvider.getIdentityProviderConnectorParams(), logoutData);
    }

    public ClientSideAuthData refresh(String refreshToken) throws ServiceException {
        return refresh(refreshToken, null);
    }

    public M2MAuthData refreshM2M(String refreshToken) throws ServiceException {
        IdentityProviderEntity identityProvider = getDomainIdentityProviderSafe();
        IdentityProviderConnector identityProviderConnector = featurerService.getFeaturer(identityProvider.getIdentityProviderConnectorFeaturerId(), IdentityProviderConnector.class);
        Trustor trustor = featurerService.getFeaturer(identityProvider.getTrustorFeaturerId(), Trustor.class);
        return new M2MAuthData()
                .setClientSideAuthData(identityProviderConnector.refresh(identityProvider.getIdentityProviderConnectorParams(), refreshToken, null))
                .setActAsUserKey(trustor.getActAsUserPublicKey(identityProvider.getTrustorParams()));
    }

    public ClientSideAuthData refresh(String refreshToken, String fingerprint) throws ServiceException {
        IdentityProviderEntity identityProvider = getDomainIdentityProviderSafe();
        IdentityProviderConnector identityProviderConnector = featurerService.getFeaturer(identityProvider.getIdentityProviderConnectorFeaturerId(), IdentityProviderConnector.class);
        return identityProviderConnector.refresh(identityProvider.getIdentityProviderConnectorParams(), refreshToken, fingerprint);
    }

    public IdentityProviderConfig getConfig() throws ServiceException {
        IdentityProviderEntity identityProvider = getDomainIdentityProviderSafe();
        IdentityProviderConnector identityProviderConnector = featurerService.getFeaturer(identityProvider.getIdentityProviderConnectorFeaturerId(), IdentityProviderConnector.class);
        IdentityProviderConfig identityProviderConfig = new IdentityProviderConfig()
                .setIdentityProvider(identityProvider)
                .setSupportedMethods(identityProviderConnector.getSupportedMethods(identityProvider.getIdentityProviderConnectorParams()));
        return identityProviderConfig;
    }

    public TokenMetaData resolveAuthTokenMetaData(String authToken) throws ServiceException {
        IdentityProviderEntity identityProvider = getDomainIdentityProviderSafe();
        IdentityProviderConnector identityProviderConnector = featurerService.getFeaturer(identityProvider.getIdentityProviderConnectorFeaturerId(), IdentityProviderConnector.class);
        return identityProviderConnector.resolveAuthTokenMetaData(identityProvider.getIdentityProviderConnectorParams(), authToken);
    }

    private static final CryptKey passwordCryptKey = new CryptKey().setExpires(LocalDateTime.now());

    public CryptKey.CryptPublicKey getPublicKeyForPasswordCrypt() throws NoSuchAlgorithmException {
        if (passwordCryptKey.getExpires().isBefore(LocalDateTime.now())) {
            passwordCryptKey.flush();
        }
        return passwordCryptKey.getPublicKey();
    }

    private String decryptPassword(String encryptedPassword, UUID keyId) throws ServiceException {

        if (passwordCryptKey.getExpires().isBefore(LocalDateTime.now())
                || keyId == null
                || !keyId.equals(passwordCryptKey.getId())) {
            throw new ServiceException(ErrorCodeTwins.IDP_INCORRECT_CRYPT_KEY);
        }
        try {
            return CryptUtils.decrypt(encryptedPassword, passwordCryptKey.getKeyPair());
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.IDP_INCORRECT_CRYPT_KEY);
        }
    }

    public EmailVerificationType signupByEmailInitiate(AuthSignup authSignup) throws ServiceException {
        IdentityProviderEntity identityProvider = getDomainIdentityProviderSafe();
        if (authSignup.getPublicKeyId() != null) {
            authSignup.setPassword(decryptPassword(authSignup.getPassword(), authSignup.getPublicKeyId()));
        }
        UserEntity user = userService.findByEmail(authSignup.getEmail());
        if (user == null) {
            user = new UserEntity()
                    .setId(UUID.randomUUID())
                    .setName(authSignup.getFirstName() + " " + authSignup.getLastName())
                    .setUserStatusId(UserStatus.EMAIL_VERIFICATION_REQUIRED);
            userService.addUser(user, EntitySmartService.SaveMode.saveAndThrowOnException);
        }
        authSignup.setTwinsUserId(user.getId());
        IdentityProviderConnector identityProviderConnector = featurerService.getFeaturer(identityProvider.getIdentityProviderConnectorFeaturerId(), IdentityProviderConnector.class);
        EmailVerificationHolder emailVerificationHolder = identityProviderConnector.signupByEmailInitiate(identityProvider.getIdentityProviderConnectorParams(), authSignup);
        UserEmailVerificationEntity userEmailVerificationEntity = new UserEmailVerificationEntity()
                .setId(UUID.randomUUID())
                .setEmail(authSignup.getEmail())
                .setIdentityProviderId(identityProvider.getId())
                .setUserId(user.getId())
                .setCreatedAt(Timestamp.from(Instant.now()));
        if (emailVerificationHolder instanceof EmailVerificationByTwins emailVerificationByTwins) {
            userEmailVerificationEntity
                    .setVerificationCodeIDP(emailVerificationByTwins.getIdpUserActivateCode());
            Event event = new Event(Events.SIGNUP_EMAIL_VERIFICATION_CODE)
                    .addContext("email.verification.code", userEmailVerificationEntity.getId().toString());
            notificationEmailService.notify(event, authSignup.getEmail());
        }
        userEmailVerificationRepository.save(userEmailVerificationEntity);
        return EmailVerificationType.BY_CODE;
    }

    //todo create scheduler to delete old UserEmailVerificationEntity

    @Transactional(rollbackFor = Throwable.class)
    public void signupByEmailConfirm(String verificationCode) throws ServiceException {
        if (StringUtils.isBlank(verificationCode) || !UuidUtils.isUUID(verificationCode)) {
            throw new ServiceException(ErrorCodeTwins.IDP_EMAIL_VERIFICATION_CODE_INCORRECT);
        }
        UserEmailVerificationEntity userEmailVerificationEntity = userEmailVerificationRepository.findById(UUID.fromString(verificationCode)).orElse(null);
        if (userEmailVerificationEntity == null) {
            throw new ServiceException(ErrorCodeTwins.IDP_EMAIL_VERIFICATION_CODE_INCORRECT);
        } else if (userEmailVerificationEntity.getCreatedAt() == null
                || userEmailVerificationEntity.getCreatedAt().before(Timestamp.from(Instant.now().minusSeconds(1800)))) { //todo move to properties
            throw new ServiceException(ErrorCodeTwins.IDP_EMAIL_VERIFICATION_CODE_EXPIRED);
        }
        IdentityProviderEntity identityProvider = checkIdentityProviderActive(userEmailVerificationEntity.getIdentityProvider());
        IdentityProviderConnector identityProviderConnector = featurerService.getFeaturer(identityProvider.getIdentityProviderConnectorFeaturerId(), IdentityProviderConnector.class);
        identityProviderConnector.signupByEmailActivate(identityProvider.getIdentityProviderConnectorParams(), userEmailVerificationEntity.getUserId(), userEmailVerificationEntity.getEmail(), verificationCode);
        UserEntity user = userEmailVerificationEntity.getUser();
        if (user.getUserStatusId() == UserStatus.EMAIL_VERIFICATION_REQUIRED) {
            //this is absolutely new twin user, it was not signed up in system before by any IDP (other domains)
            user
                    .setEmail(userEmailVerificationEntity.getEmail())
                    .setUserStatusId(UserStatus.ACTIVE);
            userService.saveSafe(user);
        }
        ApiUser apiUser = authService.getApiUser();
        apiUser.setUserResolver(new UserResolverGivenId(user.getId())); //welcome
        domainUserService.addUser(user, true);
        userEmailVerificationRepository.delete(userEmailVerificationEntity);
    }

    public ActAsUser resolveActAsUser(String actAsUserHeader) throws ServiceException {
        IdentityProviderEntity identityProvider = getDomainIdentityProviderSafe();
        Trustor trustor = featurerService.getFeaturer(identityProvider.getTrustorFeaturerId(), Trustor.class);
        return trustor.resolveActAsUser(identityProvider.getTrustorParams(), actAsUserHeader);
    }

    public void switchActiveBusinessAccount(String authToken, UUID newBusinessAccountId) throws ServiceException {
        switchActiveBusinessAccount(authToken, newBusinessAccountId, authService.getApiUser().getUserId());
    }

    public void switchActiveBusinessAccount(String authToken, UUID userId, UUID newBusinessAccountId) throws ServiceException {
        apiUserResolverService.checkDBU(authService.getApiUser().getDomainId(), newBusinessAccountId, userId);
        IdentityProviderEntity identityProvider = getDomainIdentityProviderSafe();
        IdentityProviderConnector identityProviderConnector = featurerService.getFeaturer(identityProvider.getIdentityProviderConnectorFeaturerId(), IdentityProviderConnector.class);
        identityProviderConnector.switchActiveBusinessAccount(identityProvider.getIdentityProviderConnectorParams(), authToken, authService.getApiUser().getDomainId(), newBusinessAccountId);
    }
}
