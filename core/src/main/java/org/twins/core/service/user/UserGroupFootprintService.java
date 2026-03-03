package org.twins.core.service.user;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.user.UserGroupFootprintEntity;
import org.twins.core.dao.user.UserGroupFootprintRepository;
import org.twins.core.service.auth.AuthService;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static org.cambium.common.util.SpecificationUtils.collectionUuidsToSqlArray;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class UserGroupFootprintService extends EntitySecureFindServiceImpl<UserGroupFootprintEntity> {
    final UserGroupFootprintRepository repository;
    final AuthService authService;

    @Override
    public CrudRepository<UserGroupFootprintEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<UserGroupFootprintEntity, UUID> entityGetIdFunction() {
        return UserGroupFootprintEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(UserGroupFootprintEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(UserGroupFootprintEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID getOrCreateFootprint(Set<UUID> userGroupIds) throws ServiceException {
        return repository.getOrCreateFootprint(authService.getApiUser().getDomainId(), collectionUuidsToSqlArray(userGroupIds));
    }
}
