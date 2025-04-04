package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclassfield.TwinClassFieldMotionTriggerEntity;
import org.twins.core.dao.twinclassfield.TwinClassFieldMotionTriggerRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldMotionTriggerService extends EntitySecureFindServiceImpl<TwinClassFieldMotionTriggerEntity> {
    private final TwinClassFieldMotionTriggerRepository twinClassFieldMotionTriggerRepository;

    @Override
    public CrudRepository<TwinClassFieldMotionTriggerEntity, UUID> entityRepository() {
        return twinClassFieldMotionTriggerRepository;
    }

    @Override
    public Function<TwinClassFieldMotionTriggerEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldMotionTriggerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldMotionTriggerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFieldMotionTriggerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
