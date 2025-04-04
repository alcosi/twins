package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.validator.TwinClassFieldMotionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinClassFieldMotionValidatorRuleRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldMotionValidatorService extends EntitySecureFindServiceImpl<TwinClassFieldMotionValidatorRuleEntity> {
    private final TwinClassFieldMotionValidatorRuleRepository twinClassFieldMotionValidatorRuleRepository;
    @Override
    public CrudRepository<TwinClassFieldMotionValidatorRuleEntity, UUID> entityRepository() {
        return twinClassFieldMotionValidatorRuleRepository;
    }

    @Override
    public Function<TwinClassFieldMotionValidatorRuleEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldMotionValidatorRuleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldMotionValidatorRuleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFieldMotionValidatorRuleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
