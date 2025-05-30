package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.FeaturerService;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nType;
import org.twins.core.service.i18n.I18nService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dao.factory.*;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.*;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinDelete;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.conditioner.Conditioner;
import org.twins.core.featurer.factory.filler.Filler;
import org.twins.core.featurer.factory.multiplier.Multiplier;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.draft.DraftCommitService;
import org.twins.core.service.draft.DraftService;
import org.twins.core.service.twin.TwinEraserService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static org.cambium.common.util.RowUtils.mapUuidInt;


@Service
@Slf4j
@RequiredArgsConstructor
public class TwinFactoryService extends EntitySecureFindServiceImpl<TwinFactoryEntity> {
    final TwinFactoryMultiplierRepository twinFactoryMultiplierRepository;
    final TwinFactoryMultiplierFilterRepository twinFactoryMultiplierFilterRepository;
    final TwinFactoryPipelineRepository twinFactoryPipelineRepository;
    final TwinFactoryBranchRepository twinFactoryBranchRepository;
    final TwinFactoryPipelineStepRepository twinFactoryPipelineStepRepository;
    final TwinFactoryEraserRepository twinFactoryEraserRepository;
    final TwinService twinService;
    final TwinEraserService twinEraserService;
    final TwinClassService twinClassService;
    final TwinFactoryConditionRepository twinFactoryConditionRepository;
    final TwinFactoryRepository twinFactoryRepository;
    @Lazy
    final FeaturerService featurerService;
    @Lazy
    final AuthService authService;
    @Lazy
    final DraftService draftService;
    final DraftCommitService draftCommitService;
    private final TwinflowTransitionRepository twinflowTransitionRepository;
    private final I18nService i18nService;

    @Override
    public CrudRepository<TwinFactoryEntity, UUID> entityRepository() {
        return twinFactoryRepository;
    }

    @Override
    public Function<TwinFactoryEntity, UUID> entityGetIdFunction() {
        return TwinFactoryEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return !entity.getDomainId().equals(apiUser.getDomainId());
    }

    @Override
    public boolean validateEntity(TwinFactoryEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinFactoryEntity createFactory(TwinFactoryEntity factory, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        factory
                .setDomainId(apiUser.getDomainId())
                .setNameI18NId(i18nService.createI18nAndTranslations(I18nType.TWIN_FACTORY_NAME, nameI18n).getId())
                .setDescriptionI18NId(i18nService.createI18nAndTranslations(I18nType.TWIN_FACTORY_DESCRIPTION, descriptionI18n).getId())
                .setCreatedByUserId(apiUser.getUserId())
                .setCreatedByUser(apiUser.getUser())
                .setCreatedAt(Timestamp.from(Instant.now()));
        validateEntityAndThrow(factory, EntitySmartService.EntityValidateMode.beforeSave);
        return twinFactoryRepository.save(factory);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinFactoryEntity updateFactory(TwinFactoryEntity factoryEntity, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        TwinFactoryEntity dbEntity = findEntitySafe(factoryEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateFactoryKey(factoryEntity, dbEntity, changesHelper);
        updateFactoryName(nameI18n, dbEntity, changesHelper);
        updateFactoryDescription(descriptionI18n, dbEntity, changesHelper);
        return updateSafe(dbEntity, changesHelper);
    }

    private void updateFactoryKey(TwinFactoryEntity factoryEntity, TwinFactoryEntity dbEntity, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryEntity.Fields.key, dbEntity.getKey(), factoryEntity.getKey()))
            return;
        dbEntity.setKey(factoryEntity.getKey());
    }

    private void updateFactoryName(I18nEntity nameI18n, TwinFactoryEntity dbEntity, ChangesHelper changesHelper) throws ServiceException {
        if (nameI18n == null)
            return;
        if (dbEntity.getNameI18NId() != null)
            nameI18n.setId(dbEntity.getNameI18NId());
        i18nService.saveTranslations(I18nType.TWIN_FACTORY_NAME, nameI18n);
        if (changesHelper.isChanged(TwinFactoryEntity.Fields.nameI18NId, dbEntity.getNameI18NId(), nameI18n.getId()))
            dbEntity.setNameI18NId(nameI18n.getId());
    }

    private void updateFactoryDescription(I18nEntity descriptionI18n, TwinFactoryEntity dbEntity, ChangesHelper changesHelper) throws ServiceException {
        if (descriptionI18n == null)
            return;
        if (dbEntity.getDescriptionI18NId() != null)
            descriptionI18n.setId(dbEntity.getDescriptionI18NId());
        i18nService.saveTranslations(I18nType.TWIN_FACTORY_DESCRIPTION, descriptionI18n);
        if (changesHelper.isChanged(PermissionEntity.Fields.descriptionI18NId, dbEntity.getDescriptionI18NId(), descriptionI18n.getId()))
            dbEntity.setDescriptionI18NId(descriptionI18n.getId());
    }

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
        return factoryResultUncommited;
    }

    private void runFactory(UUID factoryId, FactoryContext factoryContext) throws ServiceException {
        TwinFactoryEntity factoryEntity = findEntitySafe(factoryId);
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
        runMultipliers(factoryEntity, factoryContext);
        runPipelines(factoryEntity, factoryContext);
        runBranches(factoryEntity, factoryContext);
        runErasers(factoryEntity, factoryContext);
        factoryContext.currentFactoryBranchLevelUp();
        log.info("Factory " + factoryEntity.logShort() + " ended");
    }

    private void runMultipliers(TwinFactoryEntity factoryEntity, FactoryContext factoryContext) throws ServiceException {
        List<TwinFactoryMultiplierEntity> factoryMultiplierEntityList = twinFactoryMultiplierRepository.findByTwinFactoryId(factoryEntity.getId()); //few multipliers can be attached to one factory, because one can be used to create on grouped twin, other for create isolated new twin and so on
        log.info("Loaded " + factoryMultiplierEntityList.size() + " multipliers");
        Map<UUID, List<FactoryItem>> factoryInputTwins = groupItemsByClass(factoryContext);
        LoggerUtils.traceTreeLevelDown();
        for (TwinFactoryMultiplierEntity factoryMultiplierEntity : factoryMultiplierEntityList) {
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
            Multiplier multiplier = featurerService.getFeaturer(factoryMultiplierEntity.getMultiplierFeaturer(), Multiplier.class);
            log.info("Running multiplier[" + multiplier.getClass().getSimpleName() + "] with params: " + factoryMultiplierEntity.getMultiplierParams());
            List<FactoryItem> multiplierOutput = multiplier.multiply(factoryMultiplierEntity, multiplierInput, factoryContext);
            log.info("Result:" + multiplierOutput.size() + " factoryItems");
            LoggerUtils.traceTreeLevelDown();

            List<FactoryItem> multiplierOutputFiltered = new ArrayList<>();
            List<TwinFactoryMultiplierFilterEntity> multiplierFilters = twinFactoryMultiplierFilterRepository.findByTwinFactoryMultiplierId(factoryMultiplierEntity.getId());
            if (!multiplierFilters.isEmpty()) {
                log.info("Filtering multiplier output...");
                for (FactoryItem factoryItem : multiplierOutput) {
                    boolean allowed = false;
                    for (TwinFactoryMultiplierFilterEntity filter : multiplierFilters) {
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
        List<TwinFactoryPipelineEntity> factoryPipelineEntityList = twinFactoryPipelineRepository.findByTwinFactoryIdAndActiveTrue(factoryEntity.getId());
        log.info("Loaded " + factoryPipelineEntityList.size() + " pipelines");
        LoggerUtils.traceTreeLevelDown();
        for (TwinFactoryPipelineEntity factoryPipelineEntity : factoryPipelineEntityList) {
            log.info("Checking input for " + factoryPipelineEntity.logNormal() + " **" + factoryPipelineEntity.getDescription() + "** ");
            Set<FactoryItem> pipelineInputList = getInputItems(factoryContext, factoryPipelineEntity.getInputTwinClassId(), factoryPipelineEntity.getTwinFactoryConditionSetId(), factoryPipelineEntity.getTwinFactoryConditionInvert());
            if (CollectionUtils.isEmpty(pipelineInputList)) {
                log.info("Skipping " + factoryPipelineEntity.logShort() + " because of empty input");
                continue;
            }
            runPipelineSteps(factoryContext, factoryPipelineEntity, pipelineInputList);
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
        List<TwinFactoryBranchEntity> factoryBranchEntityList = twinFactoryBranchRepository.findByTwinFactoryIdAndActiveTrue(factoryEntity.getId());
        log.info("Loaded " + factoryBranchEntityList.size() + " branches");
        LoggerUtils.traceTreeLevelDown();
        for (TwinFactoryBranchEntity factoryBranchEntity : factoryBranchEntityList) {
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
                if (!checkCondition(pipelineStepEntity.getTwinFactoryConditionSetId(), pipelineStepEntity.getTwinFactoryConditionInvert(), pipelineInput)) {
                    log.info(stepOrder + pipelineStepEntity.logNormal() + " was skipped)");
                    continue;
                }
                Filler filler = featurerService.getFeaturer(pipelineStepEntity.getFillerFeaturer(), Filler.class);
                logMsg = stepOrder + pipelineStepEntity.logNormal();
                try {
                    filler.fill(pipelineStepEntity.getFillerParams(), pipelineInput, factoryPipelineEntity.getTemplateTwin(), logMsg);
                } catch (Exception ex) {
                    if (pipelineStepEntity.getOptional() && filler.canBeOptional()) {
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
            Set<FactoryItem> eraserInputList = getInputItems(factoryContext, eraserEntity.getInputTwinClassId(), eraserEntity.getTwinFactoryConditionSetId(), eraserEntity.getTwinFactoryConditionInvert());
            if (CollectionUtils.isEmpty(eraserInputList)) {
                log.info("Skipping {} because of empty input", eraserEntity.logShort());
                continue;
            }
            TwinFactoryEraserEntity.Action action;
            for (FactoryItem eraserInput : eraserInputList) {
                //if we are in erase mode then input twin can not be marked as candidate, it's already candidate for deletion, so me will replace action to ERASE_IRREVOCABLE
                if (factoryContext.getFactoryLauncher().isDeletion() && eraserInput.isFactoryInputItem() && eraserEntity.getEraserAction() == TwinFactoryEraserEntity.Action.ERASE_CANDIDATE)
                    action = TwinFactoryEraserEntity.Action.ERASE_IRREVOCABLE;
                else
                    action = eraserEntity.getEraserAction();
                log.info("Eraser action {} was detected for {}", action, eraserInput.logDetailed());
                eraserInput.setEraseAction(new EraseAction(action, eraserEntity.logDetailed()));
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

    @Transactional
    public FactoryResultCommited commitResult(FactoryResultUncommited factoryResultUncommited) throws ServiceException {
        if (!factoryResultUncommited.isCommittable())
            throw new ServiceException(ErrorCodeTwins.FACTORY_RESULT_LOCKED);
        if (mustBeDrafted(factoryResultUncommited)) {
            //we had to draft it cause cascade deletion can affect to many twins.
            //it's not safe to keep them all in memory
            DraftEntity draftEntity = draftService.draftFactoryResult(factoryResultUncommited);
            draftCommitService.commitNowOrInQueue(draftEntity);
            return new FactoryResultCommitedMajor().setCommitedDraftEntity(draftEntity);
        } else { //we can save result without drafting
            FactoryResultCommitedMinor factoryResultCommited = new FactoryResultCommitedMinor();
            for (TwinCreate twinCreate : factoryResultUncommited.getCreates()) {
                TwinService.TwinCreateResult twinCreateResult = twinService.createTwin(twinCreate);
                factoryResultCommited.addCreatedTwin(twinCreateResult.getCreatedTwin());
            }
            for (TwinUpdate twinUpdate : factoryResultUncommited.getUpdates()) {
                twinService.updateTwin(twinUpdate);
                factoryResultCommited.addUpdatedTwin(twinUpdate.getDbTwinEntity());
            }
            return factoryResultCommited;
        }
    }

    public boolean mustBeDrafted(FactoryResultUncommited factoryResultUncommited) {
        return CollectionUtils.isNotEmpty(factoryResultUncommited.getDeletes());
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


    public void countFactoryUsages(TwinFactoryEntity twinFactory) {
        countFactoryUsages(Collections.singletonList(twinFactory));
    }

    public void countFactoryUsages(Collection<TwinFactoryEntity> twinFactories) {
        Kit<TwinFactoryEntity, UUID> needLoad = new Kit<>(TwinFactoryEntity::getId);
        for (TwinFactoryEntity twinFactory : twinFactories) {
            if (twinFactory.getFactoryUsagesCount() == null)
                needLoad.add(twinFactory);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> twinflowTransitionCounts = mapUuidInt(twinflowTransitionRepository.countByInbuiltTwinFactoryIds(needLoad.getIdSet()));
        Map<UUID, Integer> twinFactoryBranchCounts = mapUuidInt(twinFactoryBranchRepository.countByNextTwinFactoryIds(needLoad.getIdSet()));
        Map<UUID, Integer> twinFactoryPipelineCounts = mapUuidInt(twinFactoryPipelineRepository.countByNextTwinFactoryIds(needLoad.getIdSet()));

        needLoad.getCollection().forEach(twinFactory -> {
            int twinflowTransitionCount = twinflowTransitionCounts.getOrDefault(twinFactory.getId(), 0);
            int twinFactoryBranchCount = twinFactoryBranchCounts.getOrDefault(twinFactory.getId(), 0);
            int twinFactoryPipelineCount = twinFactoryPipelineCounts.getOrDefault(twinFactory.getId(), 0);
            twinFactory.setFactoryUsagesCount(twinflowTransitionCount + twinFactoryBranchCount + twinFactoryPipelineCount);
        });
    }

    public void countFactoryPipelines(TwinFactoryEntity twinFactory) {
        countFactoryPipelines(Collections.singletonList(twinFactory));
    }

    public void countFactoryPipelines(Collection<TwinFactoryEntity> twinFactories) {
        Kit<TwinFactoryEntity, UUID> needLoad = new Kit<>(TwinFactoryEntity::getId);
        for (TwinFactoryEntity twinFactory : twinFactories) {
            if (twinFactory.getFactoryPipelinesCount() == null)
                needLoad.add(twinFactory);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> factoryPipelines = mapUuidInt(twinFactoryPipelineRepository.countByTwinFactoryIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(twinFactory -> twinFactory.setFactoryPipelinesCount(factoryPipelines.getOrDefault(twinFactory.getId(), 0)));
    }

    public void countFactoryPipelineSteps(TwinFactoryPipelineEntity twinFactoryPipeline) {
        countFactoryPipelineSteps(Collections.singletonList(twinFactoryPipeline));
    }

    public void countFactoryPipelineSteps(Collection<TwinFactoryPipelineEntity> twinFactoryPipelines) {
        Kit<TwinFactoryPipelineEntity, UUID> needLoad = new Kit<>(TwinFactoryPipelineEntity::getId);
        for (TwinFactoryPipelineEntity twinFactoryPipeline : twinFactoryPipelines) {
            if (twinFactoryPipeline.getPipelineStepsCount() == null)
                needLoad.add(twinFactoryPipeline);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> factoryPipelineSteps = mapUuidInt(twinFactoryPipelineStepRepository.countByFactoryPipelineIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(twinFactoryPipeline -> twinFactoryPipeline.setPipelineStepsCount(factoryPipelineSteps.getOrDefault(twinFactoryPipeline.getId(), 0)));
    }

    public void countFactoryMultipliers(TwinFactoryEntity twinFactory) {
        countFactoryMultipliers(Collections.singletonList(twinFactory));
    }

    public void countFactoryMultipliers(Collection<TwinFactoryEntity> twinFactories) {
        Kit<TwinFactoryEntity, UUID> needLoad = new Kit<>(TwinFactoryEntity::getId);
        for (TwinFactoryEntity twinFactory : twinFactories) {
            if (twinFactory.getFactoryMultipliersCount() == null)
                needLoad.add(twinFactory);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> factoryMultipliers = mapUuidInt(twinFactoryMultiplierRepository.countByTwinFactoryIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(twinFactory -> twinFactory.setFactoryMultipliersCount(factoryMultipliers.getOrDefault(twinFactory.getId(), 0)));
    }

    public void countFactoryBranches(TwinFactoryEntity twinFactory) {
        countFactoryBranches(Collections.singletonList(twinFactory));
    }

    public void countFactoryBranches(Collection<TwinFactoryEntity> twinFactories) {
        Kit<TwinFactoryEntity, UUID> needLoad = new Kit<>(TwinFactoryEntity::getId);
        for (TwinFactoryEntity twinFactory : twinFactories) {
            if (twinFactory.getFactoryBranchesCount() == null)
                needLoad.add(twinFactory);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> factoryBranches = mapUuidInt(twinFactoryBranchRepository.countByTwinFactoryIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(twinFactory -> twinFactory.setFactoryBranchesCount(factoryBranches.getOrDefault(twinFactory.getId(), 0)));
    }

    public void countFactoryErasers(TwinFactoryEntity twinFactory) {
        countFactoryErasers(Collections.singletonList(twinFactory));
    }

    public void countFactoryErasers(Collection<TwinFactoryEntity> twinFactories) {
        Kit<TwinFactoryEntity, UUID> needLoad = new Kit<>(TwinFactoryEntity::getId);
        for (TwinFactoryEntity twinFactory : twinFactories) {
            if (twinFactory.getFactoryErasersCount() == null)
                needLoad.add(twinFactory);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> factoryErasers = mapUuidInt(twinFactoryEraserRepository.countByTwinFactoryIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(twinFactory -> twinFactory.setFactoryErasersCount(factoryErasers.getOrDefault(twinFactory.getId(), 0)));
    }

    public void countConditionSetInFactoryPipelineUsages(TwinFactoryConditionSetEntity conditionSet) {
        countConditionSetInFactoryPipelineUsages(Collections.singletonList(conditionSet));
    }

    public void countConditionSetInFactoryPipelineUsages(Collection<TwinFactoryConditionSetEntity> conditionSetList) {
        Kit<TwinFactoryConditionSetEntity, UUID> needLoad = new Kit<>(TwinFactoryConditionSetEntity::getId);
        for (TwinFactoryConditionSetEntity conditionSet : conditionSetList) {
            if (conditionSet.getInFactoryPipelineUsagesCount() == null)
                needLoad.add(conditionSet);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> conditionSetMap = mapUuidInt(twinFactoryPipelineRepository.countByConditionSetIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(conditionSet -> conditionSet.setInFactoryPipelineUsagesCount(conditionSetMap.getOrDefault(conditionSet.getId(), 0)));
    }

    public void countConditionSetInFactoryPipelineStepUsages(TwinFactoryConditionSetEntity conditionSet) {
        countConditionSetInFactoryPipelineStepUsages(Collections.singletonList(conditionSet));
    }

    public void countConditionSetInFactoryPipelineStepUsages(Collection<TwinFactoryConditionSetEntity> conditionSetList) {
        Kit<TwinFactoryConditionSetEntity, UUID> needLoad = new Kit<>(TwinFactoryConditionSetEntity::getId);
        for (TwinFactoryConditionSetEntity conditionSet : conditionSetList) {
            if (conditionSet.getInFactoryPipelineStepUsagesCount() == null)
                needLoad.add(conditionSet);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> conditionSetMap = mapUuidInt(twinFactoryPipelineStepRepository.countByConditionSetIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(conditionSet -> conditionSet.setInFactoryPipelineStepUsagesCount(conditionSetMap.getOrDefault(conditionSet.getId(), 0)));
    }

    public void countConditionSetInFactoryMultiplierFilterUsages(TwinFactoryConditionSetEntity conditionSet) {
        countConditionSetInFactoryMultiplierFilterUsages(Collections.singletonList(conditionSet));
    }

    public void countConditionSetInFactoryMultiplierFilterUsages(Collection<TwinFactoryConditionSetEntity> conditionSetList) {
        Kit<TwinFactoryConditionSetEntity, UUID> needLoad = new Kit<>(TwinFactoryConditionSetEntity::getId);
        for (TwinFactoryConditionSetEntity conditionSet : conditionSetList) {
            if (conditionSet.getInFactoryMultiplierFilterUsagesCount() == null)
                needLoad.add(conditionSet);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> conditionSetMap = mapUuidInt(twinFactoryMultiplierFilterRepository.countByConditionSetIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(conditionSet -> conditionSet.setInFactoryMultiplierFilterUsagesCount(conditionSetMap.getOrDefault(conditionSet.getId(), 0)));
    }

    public void countConditionSetInFactoryBranchUsages(TwinFactoryConditionSetEntity conditionSet) {
        countConditionSetInFactoryBranchUsages(Collections.singletonList(conditionSet));
    }

    public void countConditionSetInFactoryBranchUsages(Collection<TwinFactoryConditionSetEntity> conditionSetList) {
        Kit<TwinFactoryConditionSetEntity, UUID> needLoad = new Kit<>(TwinFactoryConditionSetEntity::getId);
        for (TwinFactoryConditionSetEntity conditionSet : conditionSetList) {
            if (conditionSet.getInFactoryBranchUsagesCount() == null)
                needLoad.add(conditionSet);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> conditionSetMap = mapUuidInt(twinFactoryBranchRepository.countByConditionSetIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(conditionSet -> conditionSet.setInFactoryBranchUsagesCount(conditionSetMap.getOrDefault(conditionSet.getId(), 0)));
    }

    public void countConditionSetInFactoryEraserUsages(TwinFactoryConditionSetEntity conditionSet) {
        countConditionSetInFactoryEraserUsages(Collections.singletonList(conditionSet));
    }

    public void countConditionSetInFactoryEraserUsages(Collection<TwinFactoryConditionSetEntity> conditionSetList) {
        Kit<TwinFactoryConditionSetEntity, UUID> needLoad = new Kit<>(TwinFactoryConditionSetEntity::getId);
        for (TwinFactoryConditionSetEntity conditionSet : conditionSetList) {
            if (conditionSet.getInFactoryEraserUsagesCount() == null)
                needLoad.add(conditionSet);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> conditionSetMap = mapUuidInt(twinFactoryEraserRepository.countByConditionSetIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(conditionSet -> conditionSet.setInFactoryEraserUsagesCount(conditionSetMap.getOrDefault(conditionSet.getId(), 0)));
    }

    public void countFactoryMultiplierFilters(TwinFactoryMultiplierEntity multiplier) {
        countFactoryMultiplierFilters(Collections.singletonList(multiplier));
    }

    public void countFactoryMultiplierFilters(Collection<TwinFactoryMultiplierEntity> multiplierList) {
        Kit<TwinFactoryMultiplierEntity, UUID> needLoad = new Kit<>(TwinFactoryMultiplierEntity::getId);
        for (TwinFactoryMultiplierEntity multiplier : multiplierList) {
            if (multiplier.getFactoryMultiplierFiltersCount() == null)
                needLoad.add(multiplier);
        }
        if (KitUtils.isEmpty(needLoad))
            return;

        Map<UUID, Integer> mulitplierFilterMap = mapUuidInt(twinFactoryMultiplierFilterRepository.countByMultiplierIds(needLoad.getIdSet()));
        needLoad.getCollection().forEach(multiplierFilter -> multiplierFilter.setFactoryMultiplierFiltersCount(mulitplierFilterMap.getOrDefault(multiplierFilter.getId(), 0)));
    }
}
