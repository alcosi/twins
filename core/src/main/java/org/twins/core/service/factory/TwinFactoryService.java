package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinOperation;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.conditioner.Conditioner;
import org.twins.core.featurer.factory.filler.FieldLookupMode;
import org.twins.core.featurer.factory.filler.Filler;
import org.twins.core.featurer.factory.multiplier.Multiplier;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
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
        return runFactory(factoryId, factoryContext, null);
    }

    public List<TwinOperation> runFactory(UUID factoryId, FactoryContext factoryContext, String factoryRunTrace) throws ServiceException {
        TwinFactoryEntity factoryEntity = findEntitySafe(factoryId);
        return runFactory(factoryEntity, factoryContext, factoryRunTrace);
    }

    public List<TwinOperation> runFactory(TwinFactoryEntity factoryEntity, FactoryContext factoryContext, String factoryRunTrace) throws ServiceException {
        log.info("Running " + factoryEntity.logNormal() + " current trace[" + factoryRunTrace + "]");
        if (factoryRunTrace == null) factoryRunTrace = "";
        if (factoryRunTrace.contains(factoryEntity.getId().toString()))
            throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "Incorrect factory config: recursion call. Current run trace[" + factoryRunTrace + "]");
        else
            factoryRunTrace += StringUtils.isBlank(factoryRunTrace) ? factoryEntity.getId().toString() : " > " + factoryEntity.getId().toString();
        List<TwinFactoryMultiplierEntity> factoryMultiplierEntityList = twinFactoryMultiplierRepository.findByTwinFactoryId(factoryEntity.getId()); //few multipliers can be attached to one factory, because one can be used to create on grouped twin, other for create isolated new twin and so on
        log.info("Loaded " + factoryMultiplierEntityList.size() + " multipliers");
        Map<UUID, List<FactoryItem>> factoryInputTwins = groupItemsByClass(factoryContext);
        LoggerUtils.traceTreeLevelDown();
        for (TwinFactoryMultiplierEntity factoryMultiplierEntity : factoryMultiplierEntityList) {
            log.info("Checking input for " + factoryMultiplierEntity.logNormal() + " **" + factoryMultiplierEntity.getComment() + "**");
            List<FactoryItem> multiplierInput = factoryInputTwins.get(factoryMultiplierEntity.getInputTwinClassId());
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
                if (twinClassService.isInstanceOf(factoryItem.getOutput().getTwinEntity().getTwinClass(), factoryPipelineEntity.getInputTwinClassId())) {
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
                if (pipelineInput.getOutput().getTwinEntity().getId() == null)
                    pipelineInput.getOutput().getTwinEntity().setId(UUID.randomUUID()); //generating id for using in fillers (if some field must be created)
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
                    pipelineInput.getOutput().getTwinEntity()
                            .setTwinStatus(factoryPipelineEntity.getOutputTwinStatus())
                            .setTwinStatusId(factoryPipelineEntity.getOutputTwinStatusId());
                }
            }
            LoggerUtils.traceTreeLevelUp();
            if (factoryPipelineEntity.getNextTwinFactoryId() != null) {
                log.info(factoryPipelineEntity.logShort() + " has nextFactoryId configured");
                LoggerUtils.traceTreeLevelDown();
                runFactory(factoryPipelineEntity.getNextTwinFactoryId(), factoryContext, factoryRunTrace);
                LoggerUtils.traceTreeLevelUp();
            }
        }
        LoggerUtils.traceTreeLevelUp();
        log.info("Factory " + factoryEntity.logShort() + " ended");
        return factoryContext.getFactoryItemList().stream().map(FactoryItem::getOutput).toList();
    }

    private Map<UUID, List<FactoryItem>> groupItemsByClass(FactoryContext factoryContext) {
        Map<UUID, List<FactoryItem>> factoryInputTwins = new HashMap<>();
        for (FactoryItem factoryItem : factoryContext.getFactoryItemList()) {
            List<FactoryItem> twinsGroupedByClass = factoryInputTwins.computeIfAbsent(factoryItem.getOutput().getTwinEntity().getTwinClassId(), k -> new ArrayList<>());
            twinsGroupedByClass.add(factoryItem);
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


    public TwinEntity lookupTwinOfClass(FactoryItem factoryItem, UUID twinClassId, int depth) {
        if (factoryItem == null || twinClassId == null || depth > 5) return null;

        TwinEntity currentTwin = factoryItem.getTwin();
        if (currentTwin != null && twinClassId.equals(currentTwin.getTwinClassId())) return currentTwin;

        List<FactoryItem> contextItems = factoryItem.getContextFactoryItemList();
        if (!CollectionUtils.isEmpty(contextItems)) {
            for (FactoryItem subItem : contextItems) {
                TwinEntity foundTwin = lookupTwinOfClass(subItem, twinClassId, depth + 1);
                if (foundTwin != null) return foundTwin;
            }
        }

        return null;
    }

    public FieldValue lookupFieldValue(FactoryItem factoryItem, UUID twinClassFieldId, FieldLookupMode fieldLookupMode) throws ServiceException {
        FieldValue fieldValue = null;
        TwinEntity contextTwin;
        switch (fieldLookupMode) {
            case fromContextFields:
                fieldValue = factoryItem.getFactoryContext().getFields().get(twinClassFieldId);
                if (fieldValue == null)
                    throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + twinClassFieldId + "] is not present in context fields");
                break;
            case fromContextTwinFields:
                contextTwin = factoryItem.checkSingleContextTwin();
                fieldValue = twinService.getTwinFieldValue(twinService.wrapField(contextTwin, twinClassFieldId));
                if (fieldValue == null)
                    throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + twinClassFieldId + "] is not present in context fields and in context twins");
                break;
            case fromContextFieldsAndContextTwinFields:
                fieldValue = factoryItem.getFactoryContext().getFields().get(twinClassFieldId);
                if (TwinService.isFilled(fieldValue))
                    break;
                // we will look inside context twin
                contextTwin = factoryItem.checkSingleContextTwin();
                fieldValue = twinService.getTwinFieldValue(contextTwin, twinClassFieldId);
                if (TwinService.isFilled(fieldValue))
                    break;
                // we will try to look deeper
                contextTwin = factoryItem.checkSingleContextItem().checkSingleContextTwin();
                fieldValue = twinService.getTwinFieldValue(contextTwin, twinClassFieldId);
                if (!TwinService.isFilled(fieldValue))
                    throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + twinClassFieldId + "] is not present in context fields and in context twins");
                break;
            case fromContextTwinFieldsAndContextFields:
                contextTwin = factoryItem.checkSingleContextTwin();
                fieldValue = twinService.getTwinFieldValue(contextTwin, twinClassFieldId);
                if (TwinService.isFilled(fieldValue))
                    break;
                // we will try to look deeper
                contextTwin = factoryItem.checkSingleContextItem().checkSingleContextTwin();
                fieldValue = twinService.getTwinFieldValue(contextTwin, twinClassFieldId);
                if (TwinService.isFilled(fieldValue))
                    break;
                // we will look inside context fields
                fieldValue = factoryItem.getFactoryContext().getFields().get(twinClassFieldId);
                if (!TwinService.isFilled(fieldValue))
                    throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "TwinClassField[" + twinClassFieldId + "] is not present in context fields and in context twins");
                break;
        }
        return fieldValue;
    }
}
