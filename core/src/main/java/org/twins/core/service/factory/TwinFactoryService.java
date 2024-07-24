package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.factory.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryResultCommited;
import org.twins.core.domain.factory.FactoryResultUncommited;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinDelete;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.conditioner.Conditioner;
import org.twins.core.featurer.factory.filler.FieldLookupMode;
import org.twins.core.featurer.factory.filler.Filler;
import org.twins.core.featurer.factory.multiplier.Multiplier;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinEraserService;
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
    final TwinFactoryEraserRepository twinFactoryEraserRepository;
    final TwinFactoryEraserStepRepository twinFactoryEraserStepRepository;
    final TwinService twinService;
    final TwinEraserService twinEraserService;
    final TwinClassService twinClassService;
    final TwinFactoryConditionRepository twinFactoryConditionRepository;
    final TwinFactoryRepository twinFactoryRepository;
    @Lazy
    final FeaturerService featurerService;
    @Lazy
    final AuthService authService;

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

    public FactoryResultUncommited runFactory(UUID factoryId, FactoryContext factoryContext) throws ServiceException {
        runFactory(factoryId, factoryContext, null);
        FactoryResultUncommited factoryResultUncommited = new FactoryResultUncommited();
        for (FactoryItem factoryItem : factoryContext.getFactoryItemList()) {
            switch (factoryItem.getDeletionMaker()) {
                case FALSE:
                case CURRENT_ITEM_SKIPPED:
                    factoryResultUncommited.addOperation(factoryItem.getOutput());
                    continue;
                case TRUE:
                    if (factoryItem.getOutput() instanceof TwinUpdate)
                        factoryResultUncommited.addOperation(new TwinDelete(factoryItem.getTwin(), false));
                    // else we can simply skip such item, because it was created and deleted at once
                    continue;
                case GLOBALLY_LOCKED:
                    factoryResultUncommited
                            .addOperation(new TwinDelete(factoryItem.getTwin(), true))
                            .setCommittable(false); // this factory result can not be commited because of lock
            }

        }
        return factoryResultUncommited;
    }

    private void runFactory(UUID factoryId, FactoryContext factoryContext, String factoryRunTrace) throws ServiceException {
        TwinFactoryEntity factoryEntity = findEntitySafe(factoryId);
        runFactory(factoryEntity, factoryContext, factoryRunTrace);
    }

    private void runFactory(TwinFactoryEntity factoryEntity, FactoryContext factoryContext, String factoryRunTrace) throws ServiceException {
        log.info("Running " + factoryEntity.logNormal() + " current trace[" + factoryRunTrace + "]");
        if (factoryRunTrace == null) factoryRunTrace = "";
        if (factoryRunTrace.contains(factoryEntity.getId().toString()))
            throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "Incorrect factory config: recursion call. Current run trace[" + factoryRunTrace + "]");
        else
            factoryRunTrace += StringUtils.isBlank(factoryRunTrace) ? factoryEntity.getId().toString() : " > " + factoryEntity.getId().toString();
        runMultipliers(factoryEntity, factoryContext);
        runPipelines(factoryEntity, factoryContext, factoryRunTrace);
        runErasers(factoryEntity, factoryContext);
        log.info("Factory " + factoryEntity.logShort() + " ended");
    }

    private void runMultipliers(TwinFactoryEntity factoryEntity, FactoryContext factoryContext) throws ServiceException {
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
    }

    private void runPipelines(TwinFactoryEntity factoryEntity, FactoryContext factoryContext, String factoryRunTrace) throws ServiceException {
        List<TwinFactoryPipelineEntity> factoryPipelineEntityList = twinFactoryPipelineRepository.findByTwinFactoryIdAndActiveTrue(factoryEntity.getId());
        log.info("Loaded " + factoryPipelineEntityList.size() + " pipelines");
        LoggerUtils.traceTreeLevelDown();
        for (TwinFactoryPipelineEntity factoryPipelineEntity : factoryPipelineEntityList) {
            log.info("Checking input for " + factoryPipelineEntity.logNormal() + " **" + factoryPipelineEntity.getDescription() + "** ");
            List<FactoryItem> pipelineInputList = getInputItems(factoryContext, factoryPipelineEntity.getInputTwinClassId(), factoryPipelineEntity.getTwinFactoryConditionSetId(), factoryPipelineEntity.isTwinFactoryConditionInvert());
            if (CollectionUtils.isEmpty(pipelineInputList)) {
                log.info("Skipping " + factoryPipelineEntity.logShort() + " because of empty input");
                continue;
            }
            runPipelineSteps(factoryContext, factoryPipelineEntity, pipelineInputList);
            if (factoryPipelineEntity.getNextTwinFactoryId() != null) {
                log.info(factoryPipelineEntity.logShort() + " has nextFactoryId configured");
                LoggerUtils.traceTreeLevelDown();
                runFactory(factoryPipelineEntity.getNextTwinFactoryId(), factoryContext, factoryRunTrace);
                LoggerUtils.traceTreeLevelUp();
            }
        }
        LoggerUtils.traceTreeLevelUp();
    }

    private void runPipelineSteps(FactoryContext factoryContext, TwinFactoryPipelineEntity factoryPipelineEntity, List<FactoryItem> pipelineInputList) throws ServiceException {
        log.info("Running {} **{}** ", factoryPipelineEntity.logNormal(), factoryPipelineEntity.getDescription());
        List<TwinFactoryPipelineStepEntity> pipelineStepEntityList = twinFactoryPipelineStepRepository.findByTwinFactoryPipelineIdAndActiveTrueOrderByOrder(factoryPipelineEntity.getId());
        LoggerUtils.traceTreeLevelDown();
        for (FactoryItem pipelineInput : pipelineInputList) {
            log.info("Processing {}", pipelineInput.logDetailed());
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
                        log.error("Step[{}] is mandatory. Factory process will be aborted", pipelineStepEntity.getId());
                        throw ex;
                    }
                }
            }
            LoggerUtils.traceTreeLevelUp();
            if (factoryPipelineEntity.getOutputTwinStatusId() != null) {
                log.info("Pipeline output twin status[{}]", factoryPipelineEntity.getOutputTwinStatusId());
                pipelineInput.getOutput().getTwinEntity()
                        .setTwinStatus(factoryPipelineEntity.getOutputTwinStatus())
                        .setTwinStatusId(factoryPipelineEntity.getOutputTwinStatusId());
            }
        }
        LoggerUtils.traceTreeLevelUp();
    }

    private void runErasers(TwinFactoryEntity factoryEntity, FactoryContext factoryContext) throws ServiceException {
        List<TwinFactoryEraserEntity> eraserEntityList = twinFactoryEraserRepository.findByTwinFactoryIdAndActiveTrue(factoryEntity.getId());
        log.info("Loaded {} erasers", eraserEntityList.size());
        LoggerUtils.traceTreeLevelDown();
        for (TwinFactoryEraserEntity eraserEntity : eraserEntityList) {
            log.info("Checking input for {} **{}** ", eraserEntity.logNormal(), eraserEntity.getDescription());
            List<FactoryItem> eraserInputList = getInputItems(factoryContext, eraserEntity.getInputTwinClassId(), eraserEntity.getTwinFactoryConditionSetId(), eraserEntity.isTwinFactoryConditionInvert());
            if (CollectionUtils.isEmpty(eraserInputList)) {
                log.info("Skipping {} because of empty input", eraserEntity.logShort());
                continue;
            }
            runEraserSteps(factoryContext, eraserEntity, eraserInputList);
        }
        LoggerUtils.traceTreeLevelUp();
    }

    private void runEraserSteps(FactoryContext factoryContext, TwinFactoryEraserEntity factoryEraserEntity, List<FactoryItem> pipelineInputList) throws ServiceException {
        log.info("Running {} **{}** ", factoryEraserEntity.logNormal(), factoryEraserEntity.getDescription());
        List<TwinFactoryEraserStepEntity> eraserStepEntityList = twinFactoryEraserStepRepository.findByTwinFactoryEraserIdAndActiveTrueOrderByOrder(factoryEraserEntity.getId());
        LoggerUtils.traceTreeLevelDown();
        for (FactoryItem eraserInput : pipelineInputList) {
            log.info("Processing {}", eraserInput.logDetailed());
            String stepOrder;
            LoggerUtils.traceTreeLevelDown();
            for (int step = 0; step < eraserStepEntityList.size(); step++) {
                stepOrder = "Step " + (step + 1) + "/" + eraserStepEntityList.size();
                TwinFactoryEraserStepEntity eraserStepEntity = eraserStepEntityList.get(step);
                TwinFactoryEraserEntity.Action action;
                boolean passed = checkCondition(eraserStepEntity.getTwinFactoryConditionSetId(), eraserStepEntity.isTwinFactoryConditionInvert(), eraserInput);
                if (passed)
                    action = eraserStepEntity.getOnPassedTwinFactoryEraserAction();
                else
                    action = eraserStepEntity.getOnFailedTwinFactoryEraserAction();
                log.info("{} was passed[{}] detected action: {}", stepOrder, passed, action);
                switch (action) {
                    case NEXT:
                        continue;
                    case SKIP:
                        eraserInput.setDeletionMaker(FactoryItem.DeletionMarker.CURRENT_ITEM_SKIPPED);
                        continue;
                    case ERASE:
                        eraserInput.setDeletionMaker(FactoryItem.DeletionMarker.TRUE);
                        continue;
                    case RESTRICT:
                        eraserInput.setDeletionMaker(FactoryItem.DeletionMarker.GLOBALLY_LOCKED);
                        continue;
                    default:
                        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, "unknown action: " + action);
                }
            }
            LoggerUtils.traceTreeLevelUp();
        }
        LoggerUtils.traceTreeLevelUp();
    }

    private List<FactoryItem> getInputItems(FactoryContext factoryContext, UUID inputTwinClassId, UUID twinFactoryConditionSetId, boolean conditionInvert) throws ServiceException {
        List<FactoryItem> filtered = new ArrayList<>();
        for (FactoryItem factoryItem : factoryContext.getFactoryItemList()) {
            if (twinClassService.isInstanceOf(factoryItem.getOutput().getTwinEntity().getTwinClass(), inputTwinClassId)) {
                if (checkCondition(twinFactoryConditionSetId, conditionInvert, factoryItem))
                    filtered.add(factoryItem);
                else
                    log.warn("Factory item will be skipped because of unsuccessful condition check");
            }
        }
        return filtered;
    }

    @Transactional
    public FactoryResultCommited commitResult(FactoryResultUncommited factoryResultUncommited) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        FactoryResultCommited factoryResultCommited = new FactoryResultCommited();
        Set<UUID> deletionTwinIds = new HashSet<>();
        for (TwinCreate twinCreate : factoryResultUncommited.getCreates()) {
            TwinService.TwinCreateResult twinCreateResult = twinService.createTwin(apiUser, twinCreate);
            factoryResultCommited.addCreatedTwin(twinCreateResult.getCreatedTwin());
        }
        for (TwinUpdate twinUpdate : factoryResultUncommited.getUpdates()) {
            twinService.updateTwin(twinUpdate);
            factoryResultCommited.addUpdatedTwin(twinUpdate.getDbTwinEntity());
        }
        for (TwinDelete twinDelete : factoryResultUncommited.getDeletes()) {
            deletionTwinIds.add(twinDelete.getTwinEntity().getId());
            factoryResultCommited.addDeletedTwin(twinDelete.getTwinEntity());
        }
        if (!deletionTwinIds.isEmpty())
            twinEraserService.deleteTwins(deletionTwinIds);
        return factoryResultCommited;
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
