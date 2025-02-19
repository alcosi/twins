package org.twins.core.controller.rest.priv.factory;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryConditionSetRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class TwinFactoryConditionSetService extends EntitySecureFindServiceImpl<TwinFactoryConditionSetEntity> {
    private final TwinFactoryConditionSetRepository repository;

    @Override
    public CrudRepository<TwinFactoryConditionSetEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinFactoryConditionSetEntity, UUID> entityGetIdFunction() {
        return TwinFactoryConditionSetEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryConditionSetEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFactoryConditionSetEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
