package org.twins.core.service.space;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.service.TwinsEntitySecureFindService;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class SpaceRoleUserService extends TwinsEntitySecureFindService<SpaceRoleUserEntity> {
    @Getter
    private final SpaceRoleUserRepository repository;

    @Override
    public CrudRepository<SpaceRoleUserEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<SpaceRoleUserEntity, UUID> entityGetIdFunction() {
        return SpaceRoleUserEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(SpaceRoleUserEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(SpaceRoleUserEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public Set<UUID> getUsers(UUID twinId, Set<UUID> spaceRoleIds) {
        return repository.findUserIdsByTwinIdAndSpaceRoleIds(twinId, spaceRoleIds);
    }
}
