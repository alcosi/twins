package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclassfield.TwinClassFieldMotionEntity;
import org.twins.core.dao.twinclassfield.TwinClassFieldMotionRepository;
import org.twins.core.dao.twinclassfield.TwinClassFieldMotionTriggerEntity;
import org.twins.core.dao.validator.TwinClassFieldMotionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.domain.motion.FieldMotionContext;
import org.twins.core.featurer.motion.trigger.MotionTrigger;
import org.twins.core.featurer.twin.validator.TwinValidator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldMotionService extends EntitySecureFindServiceImpl<TwinClassFieldMotionEntity> {
    private final TwinClassFieldMotionRepository twinClassFieldMotionRepository;
    private final TwinClassFieldMotionTriggerService twinClassFieldMotionTriggerService;
    private final TwinClassFieldMotionValidatorService twinClassFieldMotionValidatorService;
    private final FeaturerService featurerService;

    @Override
    public CrudRepository<TwinClassFieldMotionEntity, UUID> entityRepository() {
        return twinClassFieldMotionRepository;
    }

    @Override
    public Function<TwinClassFieldMotionEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldMotionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldMotionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFieldMotionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public boolean runValidators(TwinClassFieldMotionEntity fieldMotionEntity, TwinEntity twinEntity) throws ServiceException {
        boolean validationResultOfRule = true;
        twinClassFieldMotionValidatorService.loadValidators(fieldMotionEntity);
        for (TwinClassFieldMotionValidatorRuleEntity motionValidatorRuleEntity : fieldMotionEntity.getValidatorRulesKit()) {
            validationResultOfRule = true;
            if (!motionValidatorRuleEntity.isActive()) {
                log.info("{} will not be used, since it is inactive. ", motionValidatorRuleEntity.logDetailed());
                continue;
            }
            List<TwinValidatorEntity> sortedTwinValidators = new ArrayList<>(motionValidatorRuleEntity.getTwinValidators());
            sortedTwinValidators.sort(Comparator.comparing(TwinValidatorEntity::getOrder));
            for (TwinValidatorEntity twinValidatorEntity : sortedTwinValidators) {
                if (!twinValidatorEntity.isActive()) {
                    log.info("{} from {} will not be used, since it is inactive. ", twinValidatorEntity.logNormal(), motionValidatorRuleEntity.logNormal());
                    continue;
                }
                TwinValidator transitionValidator = featurerService.getFeaturer(twinValidatorEntity.getTwinValidatorFeaturer(), TwinValidator.class);
                TwinValidator.ValidationResult validationResult = transitionValidator.isValid(twinValidatorEntity.getTwinValidatorParams(), twinEntity, twinValidatorEntity.isInvert());
                validationResultOfRule = validationResult.isValid();
                if (!validationResultOfRule) {
                    log.info("{} from {} is not valid. {}", twinValidatorEntity.logNormal(), motionValidatorRuleEntity.logNormal(), validationResult.getMessage());
                    break;
                }
            }
        }
        return validationResultOfRule;
    }

    @Transactional
    public void runTriggers(FieldMotionContext fieldMotionContext) throws ServiceException {
        TwinClassFieldMotionEntity fieldMotionEntity = fieldMotionContext.getFieldMotionEntity();
        twinClassFieldMotionTriggerService.loadTriggers(fieldMotionEntity);
        for (TwinClassFieldMotionTriggerEntity trigger : fieldMotionEntity.getTriggersKit()) {
            if (!trigger.isActive()) {
                log.info("{} will not be triggered, since it is inactive", trigger.logDetailed());
                continue;
            }
            log.info("{} will be triggered", trigger.logDetailed());
            MotionTrigger motionTrigger = featurerService.getFeaturer(trigger.getMotionTriggerFeaturerId(), MotionTrigger.class);
            motionTrigger.run(trigger.getMotionTriggerParams(), fieldMotionContext.getTwinEntity(), fieldMotionEntity.getTwinClassField());
        }
    }

    @Transactional
    public void runMotion(FieldMotionContext fieldMotionContext) throws ServiceException {
        runValidators(fieldMotionContext.getFieldMotionEntity(), fieldMotionContext.getTwinEntity());
        runTriggers(fieldMotionContext);
    }
}
