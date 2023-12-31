package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinOperation;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.conditioner.Conditioner;
import org.twins.core.featurer.factory.filler.Filler;
import org.twins.core.featurer.factory.multiplier.Multiplier;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TwinFactoryService extends EntitySecureFindServiceImpl<TwinFactoryEntity> {
    final TwinFactoryMultiplierRepository twinFactoryMultiplierRepository;
    final TwinFactoryPipelineRepository twinFactoryPipelineRepository;
    final TwinFactoryPipelineStepRepository twinFactoryPipelineStepRepository;
    final TwinService twinService;
    final TwinClassService twinClassService;
    final TwinFactoryConditionRepository twinFactoryConditionRepository;
    final TwinFactoryRepository twinFactoryRepository;
    @Lazy
    final FeaturerService featurerService;

    @Override
    public CrudRepository<TwinFactoryEntity, UUID> entityRepository() {
        return twinFactoryRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFactoryEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public List<TwinOperation> runFactory(UUID factoryId, FactoryContext factoryContext) throws ServiceException {
        TwinFactoryEntity factoryEntity = findEntitySafe(factoryId);
        return runFactory(factoryEntity, factoryContext);
    }

    public List<TwinOperation> runFactory(TwinFactoryEntity factoryEntity, FactoryContext factoryContext) throws ServiceException {
        log.info("Running " + factoryEntity.logNormal());
        List<TwinFactoryMultiplierEntity> factoryMultiplierEntityList = twinFactoryMultiplierRepository.findByTwinFactoryId(factoryEntity.getId()); //few multipliers can be attached to one factory, because one can be used to create on grouped twin, other for create isolated new twin and so on
        log.info("Loaded " + factoryMultiplierEntityList.size() + " multipliers");
        Map<UUID, List<TwinEntity>> factoryInputTwins = groupItemsByClass(factoryContext);
        LoggerUtils.traceTreeLevelDown();
        for (TwinFactoryMultiplierEntity factoryMultiplierEntity : factoryMultiplierEntityList) {
            log.info("Checking " + factoryMultiplierEntity.logDetailed());
            List<TwinEntity> multiplierInput = factoryInputTwins.get(factoryMultiplierEntity.getInputTwinClassId());
            if (CollectionUtils.isEmpty(multiplierInput)) {
                log.info("Skipping: no input of twinClass[" + factoryMultiplierEntity.getInputTwinClassId() + "]");
                continue;
            }
            Multiplier multiplier = featurerService.getFeaturer(factoryMultiplierEntity.getMultiplierFeaturer(), Multiplier.class);
            log.info("Running multiplier[" + multiplier.getClass().getSimpleName() + "] with params: " + factoryMultiplierEntity.getMultiplierParams());
            List<FactoryItem> multiplierOutput = multiplier.multiply(factoryMultiplierEntity, multiplierInput, factoryContext);
            log.info("Result:" + multiplierOutput.size() + " factoryItems");
            LoggerUtils.traceTreeLevelDown();
            for (FactoryItem factoryItem : multiplierOutput) {
                log.info(factoryItem.logDetailed());
            }
            LoggerUtils.traceTreeLevelUp();
            factoryContext.getFactoryItemList().addAll(multiplierOutput);
        }
        LoggerUtils.traceTreeLevelUp();
        List<TwinFactoryPipelineEntity> factoryPipelineEntityList = twinFactoryPipelineRepository.findByTwinFactoryIdAndActiveTrue(factoryEntity.getId());
        log.info("Loaded " + factoryPipelineEntityList.size() + " pipelines");
        LoggerUtils.traceTreeLevelDown();
        for (TwinFactoryPipelineEntity factoryPipelineEntity : factoryPipelineEntityList) {
            log.info("Checking input for " + factoryPipelineEntity.logNormal() + " **" + factoryPipelineEntity.getDescription() + "** ");
            List<FactoryItem> pipelineInputList = new ArrayList<>();
            for (FactoryItem factoryItem : factoryContext.getFactoryItemList()) {
                if (twinClassService.isInstanceOf(factoryItem.getOutputTwin().getTwinEntity().getTwinClass(), factoryPipelineEntity.getInputTwinClassId())) {
                    if (checkCondition(factoryPipelineEntity.getTwinFactoryConditionSetId(), factoryPipelineEntity.isTwinFactoryConditionInvert(), factoryItem))
                        pipelineInputList.add(factoryItem);
                    else
                        log.warn("Factory item will be skipped because of unsuccessful condition check");
                }
            }
            if (CollectionUtils.isEmpty(pipelineInputList)) {
                log.info("Skipping " + factoryPipelineEntity.logShort() + " because of empty input");
                continue;
            }
            log.info("Running " + factoryPipelineEntity.logNormal() + " **" + factoryPipelineEntity.getDescription() + "** ");
            List<TwinFactoryPipelineStepEntity> pipelineStepEntityList = twinFactoryPipelineStepRepository.findByTwinFactoryPipelineIdAndActiveTrueOrderByOrder(factoryPipelineEntity.getId());
            LoggerUtils.traceTreeLevelDown();
            for (FactoryItem pipelineInput : pipelineInputList) {
                log.info("Processing " + pipelineInput.logDetailed());
                pipelineInput.setFactoryContext(factoryContext); // setting global factory context to be accessible from fillers
                if (pipelineInput.getOutputTwin().getTwinEntity().getId() == null)
                    pipelineInput.getOutputTwin().getTwinEntity().setId(UUID.randomUUID()); //generating id for using in fillers (if some field must be created)
                String logMsg, stepOrder;
                LoggerUtils.traceTreeLevelDown();
                for (int step = 0; step < pipelineStepEntityList.size(); step++) {
                    stepOrder = "Step " + (step + 1) + "/" + pipelineStepEntityList.size() + " ";
                    TwinFactoryPipelineStepEntity pipelineStepEntity = pipelineStepEntityList.get(step);
                    if (!checkCondition(pipelineStepEntity.getTwinFactoryConditionSetId(), pipelineStepEntity.isTwinFactoryConditionInvert(), pipelineInput)) {
                        log.info(stepOrder + pipelineStepEntity.logNormal() + " was skipped)");
                        continue;
                    }
                    Filler filler = featurerService.getFeaturer(pipelineStepEntity.getFillerFeaturer(), Filler.class);
                    logMsg = stepOrder + pipelineStepEntity.logNormal();
                    try {
                        filler.fill(pipelineStepEntity.getFillerParams(), pipelineInput, factoryPipelineEntity.getTemplateTwin(), logMsg);
                    } catch (Exception ex) {
                        if (pipelineStepEntity.isOptional() && filler.canBeOptional()) {
                            log.warn("Step is optional and unsuccessful: " + (ex instanceof ServiceException serviceException ? serviceException.getErrorLocation() : ex.getMessage()) + ". Pipeline will not be aborted");
                        } else {
                            log.error("Step[" + pipelineStepEntity.getId() + "] is mandatory. Factory process will be aborted");
                            throw ex;
                        }
                    }
                }
                LoggerUtils.traceTreeLevelUp();
                if (factoryPipelineEntity.getOutputTwinStatusId() != null) {
                    log.info("Pipeline output twin status[" + factoryPipelineEntity.getOutputTwinStatusId() + "]");
                    pipelineInput.getOutputTwin().getTwinEntity()
                            .setTwinStatus(factoryPipelineEntity.getOutputTwinStatus())
                            .setTwinStatusId(factoryPipelineEntity.getOutputTwinStatusId());
                }
            }
            LoggerUtils.traceTreeLevelUp();
            if (factoryPipelineEntity.getNextTwinFactoryId() != null) {
                log.info(factoryPipelineEntity.logShort() + " has nextFactoryId configured");
                LoggerUtils.traceTreeLevelDown();
                runFactory(factoryPipelineEntity.getNextTwinFactoryId(), factoryContext); //todo endless recursion risk
                LoggerUtils.traceTreeLevelUp();
            }
        }
        LoggerUtils.traceTreeLevelUp();
        log.info("Factory " + factoryEntity.logShort() + " ended");
        return factoryContext.getFactoryItemList().stream().map(FactoryItem::getOutputTwin).toList();
    }

    private Map<UUID, List<TwinEntity>> groupItemsByClass(FactoryContext factoryContext) {
        Map<UUID, List<TwinEntity>> factoryInputTwins = new HashMap<>();
        for (FactoryItem factoryItem : factoryContext.getFactoryItemList()) {
            TwinOperation twinOperation = factoryItem.getOutputTwin();
            List<TwinEntity> twinsGroupedByClass = factoryInputTwins.computeIfAbsent(factoryItem.getOutputTwin().getTwinEntity().getTwinClassId(), k -> new ArrayList<>());
            if (twinOperation instanceof TwinUpdate twinUpdate)
                twinsGroupedByClass.add(twinUpdate.getDbTwinEntity());
            else
                twinsGroupedByClass.add(twinOperation.getTwinEntity());
        }
        return factoryInputTwins;
    }

    private boolean checkCondition(UUID conditionSetId, boolean twinFactoryConditionInvert, FactoryItem factoryItem) throws ServiceException {
        if (conditionSetId == null)
            return true;
        boolean ret = checkCondition(conditionSetId, factoryItem);
        return twinFactoryConditionInvert ? !ret : ret;
    }

    //todo result can be cached in session cache
//    @Cacheable(value = "TwinFactoryService.checkCondition", key = "{#conditionSetId, #factoryItem.hashCode() }", cacheManager = "cacheManagerRequestScope")
    public boolean checkCondition(UUID conditionSetId, FactoryItem factoryItem) throws ServiceException {
        if (conditionSetId == null)
            return true;
        List<TwinFactoryConditionEntity> conditionEntityList = twinFactoryConditionRepository.findByTwinFactoryConditionSetIdAndActiveTrue(conditionSetId);
        for (TwinFactoryConditionEntity conditionEntity : conditionEntityList) {
            Conditioner conditioner = featurerService.getFeaturer(conditionEntity.getConditionerFeaturer(), Conditioner.class);
            boolean conditionerResult = conditioner.check(conditionEntity, factoryItem);
            if (conditionEntity.isInvert())
                conditionerResult = !conditionerResult;
            if (!conditionerResult) // no need to check other conditions if one of it is already false
                return false;
        }
        return true;
    }
}
