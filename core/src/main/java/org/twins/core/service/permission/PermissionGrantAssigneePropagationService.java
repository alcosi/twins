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
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationEntity;
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationRepository;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class PermissionGrantAssigneePropagationService extends EntitySecureFindServiceImpl<PermissionGrantAssigneePropagationEntity> {
    @Getter
    private final PermissionGrantAssigneePropagationRepository repository;
    private final AuthService authService;

    @Override
    public CrudRepository<PermissionGrantAssigneePropagationEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<PermissionGrantAssigneePropagationEntity, UUID> entityGetIdFunction() {
        return PermissionGrantAssigneePropagationEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(PermissionGrantAssigneePropagationEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied=!entity.getTwinClass().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(PermissionGrantAssigneePropagationEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
            switch (entityValidateMode) {
                case beforeSave:

            }
        return true;
    }
}
