package org.twins.core.service.space;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dao.space.SpaceRoleRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class SpaceRoleService extends TwinsEntitySecureFindService<SpaceRoleEntity> {
    @Getter
    private final SpaceRoleRepository repository;

    @Lazy
    final EntitySmartService entitySmartService;

    @Lazy
    final AuthService authService;

    final SpaceRoleRepository spaceRoleRepository;

    public void forceDeleteRoles(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();

        List<UUID> rolesToDelete = spaceRoleRepository.findAllByBusinessAccountIdAndDomainId(businessAccountId, domainId);
        entitySmartService.deleteAllAndLog(rolesToDelete, spaceRoleRepository);
    }

    @Override
    public CrudRepository<SpaceRoleEntity, UUID> entityRepository() {
        return repository;
    }
    @Override
    public Function<SpaceRoleEntity, UUID> entityGetIdFunction() {
        return SpaceRoleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(SpaceRoleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(SpaceRoleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
