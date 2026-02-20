package org.twins.core.service.user;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.user.UserDelegationEntity;
import org.twins.core.dao.user.UserDelegationRepository;
import org.twins.core.dao.user.UserEntity;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class UserDelegateService extends EntitySecureFindServiceImpl<UserDelegationEntity> {

    private final UserDelegationRepository repository;

    @Override
    public CrudRepository<UserDelegationEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<UserDelegationEntity, UUID> entityGetIdFunction() {
        return UserDelegationEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(UserDelegationEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        //todo denied if user is not registered in current domain
        return false;
    }

    @Override
    public boolean validateEntity(UserDelegationEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public UserEntity findByMachineUserIdAndDomainId(UUID machineUserId, UUID domainId) {
        return repository.findByMachineUserIdAndDomainId(machineUserId, domainId);
    }
}
