package org.twins.core.service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.domain.DomainUserNoRelationProjection;
import org.twins.core.dao.domain.DomainUserRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.domain.user.DomainUserInitiator;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class DomainUserService extends EntitySecureFindServiceImpl<DomainUserEntity> {
    @Getter
    private final DomainUserRepository domainUserRepository;
    private final AuthService authService;
    private final FeaturerService featurerService;
    @Lazy
    private final DomainService domainService;
    private final UserService userService;

    @Override
    public CrudRepository<DomainUserEntity, UUID> entityRepository() {
        return domainUserRepository;
    }

    @Override
    public Function<DomainUserEntity, UUID> entityGetIdFunction() {
        return DomainUserEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DomainUserEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allowed in " + domain.logShort());
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(DomainUserEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public DomainUserEntity findByUserId(UUID userId) throws ServiceException {
        var entity = domainUserRepository.findByDomainIdAndUserId(userId, authService.getApiUser().getDomainId());
        if (entity == null)
            return null;
        if (isEntityReadDenied(entity))
            return null;
        return entity;
    }

    public void addUser(UUID userId, boolean ignoreAlreadyExists) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        DomainUserNoRelationProjection existed = getDomainUserNoRelationProjection(userId);
        if (existed != null) {
            if (ignoreAlreadyExists)
                return;
            else
                throw new ServiceException(ErrorCodeTwins.DOMAIN_USER_ALREADY_EXISTS, "user[" + userId + " is already registered in " + domain.logShort());
        }
        Locale locale = authService.getApiUser().getLocale();
        domainService.checkLocaleActiveInDomain(locale);
        DomainUserEntity domainUserEntity = new DomainUserEntity()
                .setDomainId(domain.getId())
                .setDomain(domain)
                .setUserId(userId)
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setI18nLocaleId(locale);
        DomainUserInitiator domainUserInitiator = featurerService.getFeaturer(domain.getDomainUserInitiatorFeaturerId(), DomainUserInitiator.class);
        domainUserInitiator.init(domain.getDomainUserInitiatorParams(), domainUserEntity);
    }

    public void addUserSmart(UUID userId, boolean ignoreAlreadyExists) throws ServiceException {
        userService.addUser(userId, EntitySmartService.SaveMode.ifNotPresentCreate);
        addUser(userId, ignoreAlreadyExists);
    }

    public void deleteUser(UUID userId) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        DomainUserNoRelationProjection domainUserEntity = getDomainUserNoRelationProjection(userId);
        if (domainUserEntity == null)
            throw new ServiceException(ErrorCodeTwins.DOMAIN_USER_NOT_EXISTS, "user[" + userId + "] is not registered in " + domain.logShort());
        entitySmartService.deleteAndLog(domainUserEntity.id(), domainUserRepository);
    }

    public PaginationResult<DomainEntity> findDomainListByUser(SimplePagination pagination) throws ServiceException {
        Page<DomainEntity> domainEntityList = domainUserRepository.findAllActiveDomainByUserId(authService.getApiUser().getUserId(), PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(domainEntityList, pagination);
    }

    public DomainUserNoRelationProjection getDomainUserNoRelationProjection(UUID userId) throws ServiceException {
        return domainUserRepository.findByDomainIdAndUserId(authService.getApiUser().getDomain().getId(), userId, DomainUserNoRelationProjection.class);
    }

    public DomainUserEntity getDomainUser() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return domainUserRepository.findByDomainIdAndUserId(apiUser.getDomainId(), apiUser.getUserId());
    }
}
