package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.LoggerUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.TwinChangeTaskStatus;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dao.factory.*;
import org.twins.core.dao.twin.TwinChangeTaskEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.*;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinDelete;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.factory.FactoryEraserAction;
import org.twins.core.enums.factory.FactoryLauncher;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.conditioner.Conditioner;
import org.twins.core.featurer.factory.filler.Filler;
import org.twins.core.featurer.factory.multiplier.Multiplier;
import org.twins.core.service.draft.DraftCommitService;
import org.twins.core.service.draft.DraftService;
import org.twins.core.service.trigger.TwinTriggerService;
import org.twins.core.service.twin.TwinChangeTaskService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;


@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
public class FactoryExecutionService {
    final TwinService twinService;
    final TwinClassService twinClassService;
    final TwinFactoryConditionRepository twinFactoryConditionRepository;
    final FactoryConditionSetService factoryConditionSetService;
    final FactoryPipelineService factoryPipelineService;
    @Lazy
    final FeaturerService featurerService;
    @Lazy
    final DraftService draftService;
    final DraftCommitService draftCommitService;
    @Lazy
    final TwinChangeTaskService twinChangeTaskService;
    @Lazy
    private final TwinTriggerService twinTriggerService;
    private final FactoryService factoryService;

    public FactoryResultUncommited runFactoryAndCollectResult(UUID factoryId, FactoryContext factoryContext) throws ServiceException {
        runFactory(factoryId, factoryContext);
        FactoryResultUncommited factoryResultUncommited = new FactoryResultUncommited();
        for (FactoryItem factoryItem : factoryContext.getAllFactoryItemList()) {
            if (factoryItem.getEraseAction() == null)
                continue;
            switch (factoryItem.getEraseAction().getAction()) {
                case NOT_SPECIFIED:
                    factoryResultUncommited.addOperation(factoryItem.getOutput());
                    continue;
                case ERASE_CANDIDATE:
                case ERASE_IRREVOCABLE:
                    factoryResultUncommited
                            .addOperation(new TwinDelete(factoryItem.getTwin(), factoryItem.getEraseAction()))
                            .addOperation(factoryItem.getOutput());
                    continue;
                case RESTRICT:
                    factoryResultUncommited
                            .addOperation(new TwinDelete(factoryItem.getTwin(), factoryItem.getEraseAction()))
                            .setCommittable(false); // this factory result can not be commited because of lock
            }
        }
        for (var entry : factoryContext.getAfterCommitFactories().entrySet()) {
            factoryResultUncommited.addAfterCommitFactory(entry.getKey(), entry.getValue());
        }
        return factoryResultUncommited;
    }

    private void runFactory(UUID factoryId, FactoryContext factoryContext) throws ServiceException {
        TwinFactoryEntity factoryEntity = factoryService.findEntitySafe(factoryId);
        runFactory(factoryEntity, factoryContext);
    }

    private void runFactory(TwinFactoryEntity factoryEntity, FactoryContext factoryContext) throws ServiceException {
        log.info("Running " + factoryEntity.logNormal() + " current branch[" + factoryContext.getCurrentFactoryBranchId() + "]");
        if (factoryContext.getCurrentFactoryBranchId() == null)   //we are in root factory
            factoryContext.setCurrentFactoryBranchId(FactoryBranchId.root(factoryEntity.getId()));
        else if (factoryContext.getCurrentFactoryBranchId().alreadyVisited(factoryEntity.getId()))
            throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "Incorrect factory config: recursion call. Current branch[" + factoryContext.getCurrentFactoryBranchId() + "]");
        else
            factoryContext.currentFactoryBranchLevelDown(factoryEntity.getId()); //branchId must be incremented
        factoryService.loadFactoryElements(factoryEntity);
        runMultipliers(factoryEntity, factoryContext);
        runPipelines(factoryEntity, factoryContext);
        runBranches(factoryEntity, factoryContext);
        runErasers(factoryEntity, factoryContext);
        runTriggers(factoryEntity, factoryContext);
        factoryContext.currentFactoryBranchLevelUp();
        log.info("Factory " + factoryEntity.logShort() + " ended");
    }

    private void runMultipliers(TwinFactoryEntity factoryEntity, FactoryContext factoryContext) throws ServiceException {
        Kit<TwinFactoryMultiplierEntity, UUID> factoryMultiplierEntityKit = factoryEntity.getTwinFactoryMultiplierKit();
        log.info("Loaded {} multipliers", factoryMultiplierEntityKit.size());
        if (KitUtils.isEmpty(factoryMultiplierEntityKit)) {
            return;
        }
        Map<UUID, List<FactoryItem>> factoryInputTwins = groupItemsByClass(factoryContext);
        LoggerUtils.traceTreeLevelDown();
        for (TwinFactoryMultiplierEntity factoryMultiplierEntity : factoryMultiplierEntityKit) {
            log.info("Checking input for " + factoryMultiplierEntity.logNormal() + " **" + factoryMultiplierEntity.getDescription() + "**");
            if (!factoryMultiplierEntity.getActive()) {
                log.info("Skipping: not active[" + factoryMultiplierEntity.getId() + "]");
                continue;
            }
            List<FactoryItem> multiplierInput = factoryInputTwins.get(factoryMultiplierEntity.getInputTwinClassId());
            if (CollectionUtils.isEmpty(multiplierInput)) {
                log.info("Skipping: no input of twinClass[" + factoryMultiplierEntity.getInputTwinClassId() + "]");
                continue;
            }
            Multiplier multiplier = featurerService.getFeaturer(factoryMultiplierEntity.getMultiplierFeaturerId(), Multiplier.class);
            log.info("Running multiplier[" + multiplier.getClass().getSimpleName() + "] with params: " + factoryMultiplierEntity.getMultiplierParams());
            List<FactoryItem> multiplierOutput = multiplier.multiply(factoryMultiplierEntity, multiplierInput, factoryContext);
            log.info("Result:" + multiplierOutput.size() + " factoryItems");
            LoggerUtils.traceTreeLevelDown();

            List<FactoryItem> multiplierOutputFiltered = new ArrayList<>();
            Kit<TwinFactoryMultiplierFilterEntity, UUID> multiplierFilterKit = factoryMultiplierEntity.getTwinFactoryMultiplierFilterKit();
            if (KitUtils.isNotEmpty(multiplierFilterKit)) {
                log.info("Filtering multiplier output...");
                for (FactoryItem factoryItem : multiplierOutput) {
                    boolean allowed = false;
                    for (TwinFactoryMultiplierFilterEntity filter : multiplierFilterKit) {
                        if (!filter.getInputTwinClassId().equals(factoryItem.getOutput().getTwinEntity().getTwinClassId()))
                            continue;
                        if (filter.isActive()) {
                            allowed = checkCondition(filter.getTwinFactoryConditionSetId(), filter.isTwinFactoryConditionInvert(), factoryItem);
                            if (allowed)
                                break;
                        }
                    }
                    if (allowed) {
                        log.info(factoryItem.logDetailed());
                        multiplierOutputFiltered.add(factoryItem);
                    } else
                        log.info(factoryItem.logNormal() + " was skipped");
                }
                log.info("Filtered result:" + multiplierOutputFiltered.size() + " factoryItems");
            } else
                multiplierOutputFiltered.addAll(multiplierOutput);
            LoggerUtils.traceTreeLevelUp();
            factoryContext.addAll(multiplierOutputFiltered);
        }
        LoggerUtils.traceTreeLevelUp();
    }

    private void runPipelines(TwinFactoryEntity factoryEntity, FactoryContext factoryContext) throws ServiceException {
        Kit<TwinFactoryPipelineEntity, UUID> factoryPipelineEntityKit = factoryEntity.getTwinFactoryPipelineKit();
        log.info("Loaded {} pipelines", factoryPipelineEntityKit.size());
        if (KitUtils.isEmpty(factoryPipelineEntityKit)) {
            return;
        }
        factoryPipelineService.loadTemplateTwin(factoryPipelineEntityKit.getCollection());
        factoryPipelineService.loadOutputTwinStatus(factoryPipelineEntityKit.getCollection());
        LoggerUtils.traceTreeLevelDown();
        for (TwinFactoryPipelineEntity factoryPipelineEntity : factoryPipelineEntityKit) {
            if (!Boolean.TRUE.equals(factoryPipelineEntity.getActive())) {
                log.info("Skipping inactive {}", factoryPipelineEntity.logNormal());
                continue;
            }
            log.info("Checking input for " + factoryPipelineEntity.logNormal() + " **" + factoryPipelineEntity.getDescription() + "** ");
            Set<FactoryItem> pipelineInputList = getInputItems(factoryContext, factoryPipelineEntity.getInputTwinClassId(), factoryPipelineEntity.getTwinFactoryConditionSetId(), factoryPipelineEntity.getTwinFactoryConditionInvert());
            if (CollectionUtils.isEmpty(pipelineInputList)) {
                log.info("Skipping " + factoryPipelineEntity.logShort() + " because of empty input");
                continue;
            }
            runPipelineSteps(factoryContext, factoryPipelineEntity, pipelineInputList);
            if (factoryPipelineEntity.getAfterCommitTwinFactoryId() != null) {
                factoryContext.addAfterCommitFactories(pipelineInputList, factoryPipelineEntity.getAfterCommitTwinFactoryId());
            }
            if (factoryPipelineEntity.getNextTwinFactoryId() != null) {
                log.info("{} has nextFactoryId configured", factoryPipelineEntity.logShort());
                if (factoryPipelineEntity.getNextTwinFactoryLimitScope()) {
                    //we will limit next factory items access only by current pipeline items
                    factoryContext.currentFactoryBranchEnterPipeline(factoryPipelineEntity.getId());
                    factoryContext.snapshotPipelineScope(pipelineInputList);
                }
                LoggerUtils.traceTreeLevelDown();
                runFactory(factoryPipelineEntity.getNextTwinFactoryId(), factoryContext);
                if (factoryPipelineEntity.getNextTwinFactoryLimitScope()) {
                    factoryContext.evictPipelineScope(); // we can clear it here
                    factoryContext.currentFactoryBranchExitPipeline();
                }
                LoggerUtils.traceTreeLevelUp();
            }
        }
        LoggerUtils.traceTreeLevelUp();
    }

    private void runBranches(TwinFactoryEntity factoryEntity, FactoryContext factoryContext) throws ServiceException {
        Kit<TwinFactoryBranchEntity, UUID> factoryBranchEntityKit = factoryEntity.getTwinFactoryBranchKit();
        log.info("Loaded {} branches", factoryBranchEntityKit.size());
        if (KitUtils.isEmpty(factoryBranchEntityKit)) {
            return;
        }
        LoggerUtils.traceTreeLevelDown();
        for (TwinFactoryBranchEntity factoryBranchEntity : factoryBranchEntityKit) {
            if (!Boolean.TRUE.equals(factoryBranchEntity.getActive())) {
                log.info("Skipping inactive {}", factoryBranchEntity.logNormal());
                continue;
            }
            log.info("Checking input for " + factoryBranchEntity.logNormal() + " **" + factoryBranchEntity.getDescription() + "** ");
            boolean selected = false;
            for (FactoryItem factoryItem : factoryContext.getFactoryItemList()) {
                if (checkCondition(factoryBranchEntity.getTwinFactoryConditionSetId(), factoryBranchEntity.getTwinFactoryConditionInvert(), factoryItem)) {
                    selected = true;
                    log.info("Branch was selected because of success condition check for {}", factoryItem.toString());
                    break;
                }
            }
            if (!selected) {
                log.info("Skipping " + factoryBranchEntity.logShort());
                continue;
            }
            log.info("{} has nextFactoryId configured", factoryBranchEntity.logShort());
            LoggerUtils.traceTreeLevelDown();
            runFactory(factoryBranchEntity.getNextTwinFactoryId(), factoryContext);
            LoggerUtils.traceTreeLevelUp();

        }
        LoggerUtils.traceTreeLevelUp();
    }

    private void runPipelineSteps(FactoryContext factoryContext, TwinFactoryPipelineEntity factoryPipelineEntity, Set<FactoryItem> pipelineInputList) throws ServiceException {
        log.info("Running {} **{}** ", factoryPipelineEntity.logNormal(), factoryPipelineEntity.getDescription());
        Kit<TwinFactoryPipelineStepEntity, UUID> pipelineStepEntityKit = factoryPipelineEntity.getTwinFactoryPipelineStepKit();
        LoggerUtils.traceTreeLevelDown();
        List<TwinFactoryPipelineStepEntity> pipelineStepEntityList = pipelineStepEntityKit.getList();
        factoryPipelineService.loadTemplateTwin(factoryPipelineEntity);
        factoryPipelineService.loadOutputTwinStatus(factoryPipelineEntity);
        for (FactoryItem pipelineInput : pipelineInputList) {
            log.info("Processing {}", pipelineInput.logDetailed());
            pipelineInput.setFactoryContext(factoryContext); // setting global factory context to be accessible from fillers
            if (pipelineInput.getOutput().getTwinEntity().getId() == null)
                pipelineInput.getOutput().getTwinEntity().setId(UuidUtils.generate()); //generating id for using in fillers (if some field must be created)
            String logMsg, stepOrder;
            LoggerUtils.traceTreeLevelDown();
            for (int step = 0; step < pipelineStepEntityList.size(); step++) {
                stepOrder = "Step " + (step + 1) + "/" + pipelineStepEntityList.size() + " ";
                TwinFactoryPipelineStepEntity pipelineStepEntity = pipelineStepEntityList.get(step);
                if (!Boolean.TRUE.equals(pipelineStepEntity.getActive())) {
                    log.info("Skipping inactive {}", pipelineStepEntity.logNormal());
                    continue;
                }
                if (!checkCondition(pipelineStepEntity.getTwinFactoryConditionSetId(), pipelineStepEntity.getTwinFactoryConditionInvert(), pipelineInput)) {
                    log.info(stepOrder + pipelineStepEntity.logNormal() + " was skipped)");
                    continue;
                }
                Filler filler = featurerService.getFeaturer(pipelineStepEntity.getFillerFeaturerId(), Filler.class);
                logMsg = stepOrder + pipelineStepEntity.logNormal();
                try {
                    filler.fill(pipelineStepEntity.getFillerParams(), pipelineInput, factoryPipelineEntity.getTemplateTwin(), logMsg);
                } catch (Exception ex) {
                    if (pipelineStepEntity.getOptional() && filler.canBeOptional()) {
                        log.warn("Step is optional and unsuccessful: " + (ex instanceof ServiceException serviceException ? serviceException.getErrorLocation() : ex.getMessage()) + ". Pipeline will not be aborted");
                    } else {
                        log.error("Step[{}] is mandatory. Factory process will be aborted", pipelineStepEntity.getId());
                        LoggerUtils.traceTreeEnd();
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
        Kit<TwinFactoryEraserEntity, UUID> eraserEntityKit = factoryEntity.getTwinFactoryEraserKit();
        log.info("Loaded {} erasers", eraserEntityKit.size());
        if (KitUtils.isEmpty(eraserEntityKit)) {
            return;
        }
        LoggerUtils.traceTreeLevelDown();
        for (TwinFactoryEraserEntity eraserEntity : eraserEntityKit) {
            if (!Boolean.TRUE.equals(eraserEntity.getActive())) {
                log.info("Skipping inactive {}", eraserEntity.logNormal());
                continue;
            }
            log.info("Checking input for {} **{}** ", eraserEntity.logNormal(), eraserEntity.getDescription());
            Set<FactoryItem> eraserInputList = getInputItems(factoryContext, eraserEntity.getInputTwinClassId(), eraserEntity.getTwinFactoryConditionSetId(), eraserEntity.getTwinFactoryConditionInvert());
            if (CollectionUtils.isEmpty(eraserInputList)) {
                log.info("Skipping {} because of empty input", eraserEntity.logShort());
                continue;
            }
            FactoryEraserAction action;
            for (FactoryItem eraserInput : eraserInputList) {
                //if we are in erase mode then input twin can not be marked as candidate, it's already candidate for deletion, so me will replace action to ERASE_IRREVOCABLE
                if (factoryContext.getFactoryLauncher().isDeletion() && eraserInput.isFactoryInputItem() && eraserEntity.getEraserAction() == FactoryEraserAction.ERASE_CANDIDATE)
                    action = FactoryEraserAction.ERASE_IRREVOCABLE;
                else
                    action = eraserEntity.getEraserAction();
                log.info("Eraser action {} was detected for {}", action, eraserInput.logDetailed());
                eraserInput.setEraseAction(new EraseAction(action, eraserEntity.logDetailed()));
            }
        }
        LoggerUtils.traceTreeLevelUp();
    }

    private void runTriggers(TwinFactoryEntity factoryEntity, FactoryContext factoryContext) throws ServiceException {
        Kit<TwinFactoryTriggerEntity, UUID> factoryTriggerEntityKit = factoryEntity.getTwinFactoryTriggerKit();
        log.info("Loaded {} triggers", factoryTriggerEntityKit.size());
        if (KitUtils.isEmpty(factoryTriggerEntityKit)) {
            return;
        }
        LoggerUtils.traceTreeLevelDown();
        for (TwinFactoryTriggerEntity factoryTriggerEntity : factoryTriggerEntityKit) {
            if (!Boolean.TRUE.equals(factoryTriggerEntity.getActive())) {
                log.info("Skipping inactive {}", factoryTriggerEntity.logNormal());
                continue;
            }
            Set<FactoryItem> triggerInputList = getInputItems(factoryContext, factoryTriggerEntity.getInputTwinClassId(),
                    factoryTriggerEntity.getTwinFactoryConditionSetId(), factoryTriggerEntity.getTwinFactoryConditionInvert());
            if (CollectionUtils.isEmpty(triggerInputList)) {
                log.info("Skipping trigger {} because of empty input", factoryTriggerEntity.logShort());
                continue;
            }
            for (FactoryItem triggerInput : triggerInputList) {
                TwinEntity targetTwin = triggerInput.getTwin();
                if (targetTwin == null || targetTwin.getId() == null) {
                    log.info("Skipping trigger {} because twin is not persisted yet", factoryTriggerEntity.logShort());
                    continue;
                }
                if (factoryTriggerEntity.getAsync()) {
                    factoryContext.getPostponedTriggers().add(
                            targetTwin.getId(),
                            targetTwin.getTwinStatusId(),
                            factoryTriggerEntity.getTwinTriggerId()
                    );
                } else {
                    log.info("Executing sync trigger for {} twin[{}]", factoryTriggerEntity.logNormal(), targetTwin.logShort());
                    twinTriggerService.runTriggerSync(factoryTriggerEntity.getTwinTrigger(), targetTwin, targetTwin.getTwinStatus(), null);
                }
            }
        }
        LoggerUtils.traceTreeLevelUp();
    }

    private Set<FactoryItem> getInputItems(FactoryContext factoryContext, UUID inputTwinClassId, UUID twinFactoryConditionSetId, boolean conditionInvert) throws ServiceException {
        Set<FactoryItem> filtered = new HashSet<>();
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

    @Transactional(rollbackFor = Throwable.class)
    public FactoryResultCommited commitResult(FactoryResultUncommited factoryResultUncommited) throws ServiceException {
        if (!factoryResultUncommited.isCommittable())
            throw new ServiceException(ErrorCodeTwins.FACTORY_RESULT_LOCKED);
        if (mustBeDrafted(factoryResultUncommited)) {
            //we had to draft it cause cascade deletion can affect to many twins.
            //it's not safe to keep them all in memory
            DraftEntity draftEntity = draftService.draftFactoryResult(factoryResultUncommited);
            draftCommitService.commitNowOrInQueue(draftEntity);
            return new FactoryResultCommitedMajor().setCommitedDraftEntity(draftEntity);
        } else { //we can save a result without drafting
            FactoryResultCommitedMinor factoryResultCommited = new FactoryResultCommitedMinor();
            for (TwinCreate twinCreate : factoryResultUncommited.getCreates()) {
                TwinService.TwinCreateResult twinCreateResult = twinService.createTwin(twinCreate);
                factoryResultCommited.addCreatedTwin(twinCreateResult.getCreatedTwin());
            }
            for (TwinUpdate twinUpdate : factoryResultUncommited.getUpdates()) {
                twinService.updateTwin(twinUpdate);
                factoryResultCommited.addUpdatedTwin(twinUpdate.getDbTwinEntity());
            }
            List<TwinChangeTaskEntity> changeTaskList = new ArrayList<>();
            for (var entry : factoryResultUncommited.getAfterCommitFactories().entrySet()) {
                changeTaskList.add(new TwinChangeTaskEntity()
                        .setTwinId(entry.getKey())
                        .setTwinFactoryId(entry.getValue())
                        .setTwinFactorylauncher(FactoryLauncher.factoryPipeline)
                        .setStatusId(TwinChangeTaskStatus.NEED_START));
            }
            twinChangeTaskService.addTasks(changeTaskList);
            return factoryResultCommited;
        }
    }

    public boolean mustBeDrafted(FactoryResultUncommited factoryResultUncommited) {
        return KitUtils.isNotEmpty(factoryResultUncommited.getDeletes());
    }

    private Map<UUID, List<FactoryItem>> groupItemsByClass(FactoryContext factoryContext) {
        Map<UUID, List<FactoryItem>> factoryInputTwins = new HashMap<>();
        for (FactoryItem factoryItem : factoryContext.getFactoryItemList()) {
            for (UUID twinClassId : factoryItem.getOutput().getTwinEntity().getTwinClass().getExtendedClassIdSet()) {
                List<FactoryItem> twinsGroupedByClass = factoryInputTwins.computeIfAbsent(twinClassId, k -> new ArrayList<>());
                twinsGroupedByClass.add(factoryItem);
            }
        }
        return factoryInputTwins;
    }

    private boolean checkCondition(UUID conditionSetId, boolean twinFactoryConditionInvert, FactoryItem factoryItem) throws ServiceException {
        if (conditionSetId == null)
            return true;
        boolean ret = checkCondition(conditionSetId, factoryItem);
        return twinFactoryConditionInvert ? !ret : ret;
    }

    public boolean checkCondition(UUID conditionSetId, FactoryItem factoryItem) throws ServiceException {
        if (conditionSetId == null)
            return true;
        // Check cache first
        if (factoryItem.hasConditionSetResult(conditionSetId)) {
            return factoryItem.getCachedConditionSetResult(conditionSetId);
        }
        // Evaluate and cache (only if cachable)
        List<TwinFactoryConditionEntity> conditionEntityList = twinFactoryConditionRepository.findByTwinFactoryConditionSetIdAndActiveTrue(conditionSetId);
        boolean result = true;
        for (TwinFactoryConditionEntity conditionEntity : conditionEntityList) {
            Conditioner conditioner = featurerService.getFeaturer(conditionEntity.getConditionerFeaturerId(), Conditioner.class);
            boolean conditionerResult = conditioner.check(conditionEntity, factoryItem);
            if (conditionEntity.getInvert())
                conditionerResult = !conditionerResult;
            if (!conditionerResult) {
                result = false;
                break;
            }
        }
        cacheConditionSetResultIfNeeded(conditionSetId, factoryItem, result);
        return result;
    }

    private void cacheConditionSetResultIfNeeded(UUID conditionSetId, FactoryItem factoryItem, boolean result) throws ServiceException {
        TwinFactoryConditionSetEntity conditionSet = factoryConditionSetService.findEntitySafe(conditionSetId);
        if (Boolean.TRUE.equals(conditionSet.getCachable())) {
            factoryItem.setConditionSetResult(conditionSetId, result);
        }
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
}
