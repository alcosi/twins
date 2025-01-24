package org.twins.core.service.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.permission.PermissionGrantSpaceRoleEntity;
import org.twins.core.dao.permission.PermissionGrantSpaceRoleRepository;
import org.twins.core.dao.permission.PermissionGrantTwinRoleEntity;
import org.twins.core.dao.permission.PermissionGrantTwinRoleRepository;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class PermissionGrantTwinRoleService extends EntitySecureFindServiceImpl<PermissionGrantTwinRoleEntity> {
    @Getter
    private final PermissionGrantTwinRoleRepository repository;
    private final AuthService authService;

    @Override
    public CrudRepository<PermissionGrantTwinRoleEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<PermissionGrantTwinRoleEntity, UUID> entityGetIdFunction() {
        return PermissionGrantTwinRoleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(PermissionGrantTwinRoleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied=!entity.getPermissionSchema().getDomainId().equals(domain.getId())&&
                !entity.getTwinClass().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(PermissionGrantTwinRoleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return !isEntityReadDenied(entity,EntitySmartService.ReadPermissionCheckMode.none);
    }
}
