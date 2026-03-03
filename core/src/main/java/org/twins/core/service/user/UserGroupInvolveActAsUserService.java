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
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.usergroup.UserGroupInvolveActAsUserEntity;
import org.twins.core.dao.usergroup.UserGroupInvolveActAsUserRepository;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class UserGroupInvolveActAsUserService extends EntitySecureFindServiceImpl<UserGroupInvolveActAsUserEntity> {

    private final UserGroupInvolveActAsUserRepository repository;

    @Override
    public CrudRepository<UserGroupInvolveActAsUserEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<UserGroupInvolveActAsUserEntity, UUID> entityGetIdFunction() {
        return UserGroupInvolveActAsUserEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(UserGroupInvolveActAsUserEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        //todo denied if user is not registered in current domain
        return false;
    }

    @Override
    public boolean validateEntity(UserGroupInvolveActAsUserEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public List<UserGroupEntity> findByMachineUserIdAndDomainId(UUID machineUserId, UUID domainId) {
        return repository.findByMachineUserIdAndDomainId(machineUserId, domainId);
    }

}
