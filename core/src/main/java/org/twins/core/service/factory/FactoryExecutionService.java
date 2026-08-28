package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.KitUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.TwinChangeTaskStatus;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.twin.TwinChangeTaskEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.*;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinDelete;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.factory.FactoryLauncher;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.factoryprocessor.FactoryProcessor;
import org.twins.core.service.draft.DraftCommitService;
import org.twins.core.service.draft.DraftService;
import org.twins.core.service.twin.TwinChangeTaskService;
import org.twins.core.service.twin.TwinService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
public class FactoryExecutionService {
    final TwinService twinService;
    @Lazy
    final FeaturerService featurerService;
    @Lazy
    final DraftService draftService;
    final DraftCommitService draftCommitService;
    @Lazy
    final TwinChangeTaskService twinChangeTaskService;
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

    public void runFactory(UUID factoryId, FactoryContext factoryContext) throws ServiceException {
        TwinFactoryEntity factoryEntity = factoryService.findEntitySafe(factoryId);
        runFactory(factoryEntity, factoryContext);
    }

    private void runFactory(TwinFactoryEntity factoryEntity, FactoryContext factoryContext) throws ServiceException {
        log.info("Running {} current branch[{}]", factoryEntity.logNormal(), factoryContext.getCurrentFactoryBranchId());
        FactoryProcessor factoryProcessor = featurerService.getFeaturer(factoryEntity.getFactoryProcessorFeaturerId(), FactoryProcessor.class);
        factoryProcessor.process(factoryEntity, factoryContext);
        log.info("Factory {} ended", factoryEntity.logShort());
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
