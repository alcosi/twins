package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclassfield.TwinClassFieldMotionEntity;
import org.twins.core.dao.validator.TwinClassFieldMotionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinClassFieldMotionValidatorRuleRepository;

import java.util.Collection;
import java.util.Collections;
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

    public void loadValidators(TwinClassFieldMotionEntity fieldMotion) {
        loadValidators(Collections.singleton(fieldMotion));
    }

    public void loadValidators(Collection<TwinClassFieldMotionEntity> fieldMotions) {
        Kit<TwinClassFieldMotionEntity, UUID> needLoad = new Kit<>(TwinClassFieldMotionEntity::getId);
        for (TwinClassFieldMotionEntity fieldMotion : fieldMotions) {
            if (fieldMotion.getValidatorRulesKit() != null)
                continue;
            needLoad.add(fieldMotion);
        }
        if (needLoad.isEmpty()) return;
        KitGrouped<TwinClassFieldMotionValidatorRuleEntity, UUID, UUID> validatorsKit = new KitGrouped<>(
                twinClassFieldMotionValidatorRuleRepository.findAllByFieldMotionIdInOrderByOrder(needLoad.getIdSet()), TwinClassFieldMotionValidatorRuleEntity::getId, TwinClassFieldMotionValidatorRuleEntity::getFieldMotionId);
        for (TwinClassFieldMotionEntity entry : needLoad)
            entry.setValidatorRulesKit(new Kit<>(validatorsKit.getGrouped(entry.getId()), TwinClassFieldMotionValidatorRuleEntity::getId));
    }
}
