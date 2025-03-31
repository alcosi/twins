package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinflow.TwinflowTransitionTypeEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionTypeRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowTransitionTypeService extends EntitySecureFindServiceImpl<TwinflowTransitionTypeEntity> {

    @Override
    public CrudRepository<TwinflowTransitionTypeEntity, UUID> entityRepository() {
        return null;
    }

    @Override
    public Function<TwinflowTransitionTypeEntity, UUID> entityGetIdFunction() {
        return null;
    }

    @Override
    public boolean isEntityReadDenied(TwinflowTransitionTypeEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinflowTransitionTypeEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
