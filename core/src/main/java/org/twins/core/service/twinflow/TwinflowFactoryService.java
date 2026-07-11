package org.twins.core.service.twinflow;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.KitUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dao.twinflow.TwinflowFactoryRepository;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.factory.FactoryBranchId;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryResultUncommited;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinCreateStage;
import org.twins.core.domain.twinoperation.TwinSave;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.factory.FactoryLauncher;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.factory.FactoryExecutionService;
import org.twins.core.service.twin.TwinService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinflowFactoryService extends EntitySecureFindServiceImpl<TwinflowFactoryEntity> {

    private final TwinflowService twinflowService;
    @Lazy
    private final FactoryExecutionService twinFactoryService;
    @Lazy
    private final TwinService twinService;
    private final TwinflowFactoryRepository repository;
    private final TwinflowFactoryRepository twinflowFactoryRepository;
    private final AuthService authService;
    @Value("${twins.onsave.factory.cascade.max-depth:5}")
    private int maxCascadeDepth;

    @Override
    public CrudRepository<TwinflowFactoryEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinflowFactoryEntity, UUID> entityGetIdFunction() {
        return TwinflowFactoryEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinflowFactoryEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return twinflowService.isEntityReadDenied(entity.getTwinflow(), readPermissionCheckMode)
                || twinFactoryService.isEntityReadDenied(entity.getTwinFactory(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinflowFactoryEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        switch (entityValidateMode) {
            case beforeSave -> {
                if (entity.getTwinflowId() == null) {
                    return logErrorAndReturnFalse(entity.logNormal() + " empty twinflowId");
                }

                if (entity.getTwinFactoryId() == null) {
                    return logErrorAndReturnFalse(entity.logNormal() + " empty twinFactoryId");
                }

                if (entity.getTwinFactoryLauncher() == null) {
                    return logErrorAndReturnFalse(entity.logNormal() + " empty factoryLauncher");
                }

                // for save
                if (entity.getId() == null && twinflowFactoryRepository.existsByTwinflowIdAndTwinFactoryLauncher(entity.getTwinflowId(), entity.getTwinFactoryLauncher())) {
                    return logErrorAndReturnFalse(entity.logNormal() + " already exists");
                }

                // for update
                if (entity.getId() != null && twinflowFactoryRepository.existsByTwinflowIdAndTwinFactoryLauncherAndIdNot(entity.getTwinflowId(), entity.getTwinFactoryLauncher(), entity.getId())) {
                    return logErrorAndReturnFalse(entity.logNormal() + " conflicts with existing record");
                }

                if (entity.getTwinflow() == null || !entity.getTwinflow().getId().equals(entity.getTwinflowId())) {
                    try {
                        entity.setTwinflow(twinflowService.findEntitySafe(entity.getTwinflowId()));
                    } catch (ServiceException e) {
                        return logErrorAndReturnFalse("Twinflow with id[" + entity.getTwinflowId() + "] does not exist");
                    }
                }

                if (entity.getTwinFactory() == null || !entity.getTwinFactory().getId().equals(entity.getTwinFactoryId())) {
                    try {
                        entity.setTwinFactory(twinFactoryService.findEntitySafe(entity.getTwinFactoryId()));
                    } catch (ServiceException e) {
                        return logErrorAndReturnFalse("TwinFactory with id[" + entity.getTwinFactoryId() + "] does not exist");
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean validateEntities(Collection<TwinflowFactoryEntity> entities, EntitySmartService.EntityValidateMode entityValidateMode) {
        try {
            Kit<TwinflowEntity, UUID> twinflowKit = twinflowService.findEntitiesSafe(entities.stream().map(TwinflowFactoryEntity::getTwinflowId).toList());

            for (var entity : entities) {
                entity.setTwinflow(twinflowKit.get(entity.getTwinflowId()));
            }
        } catch (ServiceException e) {
            return logErrorAndReturnFalse("List contains invalid twinflow id");
        }

        try {
            Kit<TwinFactoryEntity, UUID> twinFactoryKit = twinFactoryService.findEntitiesSafe(entities.stream().map(TwinflowFactoryEntity::getTwinFactoryId).toList());

            for (var entity : entities) {
                entity.setTwinFactory(twinFactoryKit.get(entity.getTwinFactoryId()));
            }
        } catch (ServiceException e) {
            return logErrorAndReturnFalse("List contains invalid twin factory id");
        }

        if (!existsNone(entities)) {
            return logErrorAndReturnFalse("Entity from list already exists in db");
        }

        return true;
    }

    public void loadFactories(TwinflowEntity twinflows) {
        loadFactories(Collections.singletonList(twinflows));
    }

    public void loadFactories(Collection<TwinflowEntity> twinflows) {
        Kit<TwinflowEntity, UUID> needLoad = null;
        for (var twinflow : twinflows) {
            if (twinflow.getFactoriesKit() != null)
                continue;
            if (needLoad == null)
                needLoad = new Kit<>(TwinflowEntity::getId);
            needLoad.add(twinflow);
        }
        if (KitUtils.isEmpty(needLoad))
            return;
        KitGrouped<TwinflowFactoryEntity, UUID, UUID> loaded = new KitGrouped<>(repository.findByTwinflowIdIn(needLoad.getIdSet()), TwinflowFactoryEntity::getId, TwinflowFactoryEntity::getTwinflowId);
        for (var twinflow : needLoad) {
            if (loaded.containsGroupedKey(twinflow.getId())) {
                twinflow.setFactoriesKit(new Kit<>(loaded.getGrouped(twinflow.getId()), TwinflowFactoryEntity::getTwinFactoryLauncher));
            } else {
                twinflow.setFactoriesKit(Kit.emptyKit());
            }
        }
    }

    public void runFactoryOn(TwinSave twinSave, FactoryLauncher factoryLauncher, TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinEntity twinEntity = detectTwinEntity(twinSave);
        twinflowService.loadTwinflow(twinEntity);
        loadFactories(twinEntity.getTwinflow());
        TwinflowFactoryEntity twinflowFactory = twinEntity.getTwinflow().getFactoriesKit().get(factoryLauncher);
        runFactoryOn(twinSave, twinflowFactory, twinChangesCollector);
    }

    public void runFactoryOn(TwinSave twinSave, TwinflowFactoryEntity twinflowFactory, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinflowFactory == null)
            return;
        FactoryContext factoryContext = new FactoryContext(twinflowFactory.getTwinFactoryLauncher(), FactoryBranchId.root(twinflowFactory.getTwinFactoryId()));
        factoryContext.setRequestId(authService.getApiUser().getRequestId());
        factoryContext.add(new FactoryItem().setOutput(twinSave).setFactoryContext(factoryContext));
        FactoryResultUncommited result = twinFactoryService.runFactoryAndCollectResult(twinflowFactory.getTwinFactoryId(), factoryContext);
        cascadeApplyExtras(twinSave, result, twinflowFactory, twinChangesCollector);
    }

    // The on-create/on-update factory may produce extra create/update/delete operations beyond the
    // input twin. These are applied recursively via twinService.createTwin/updateTwin, each of which
    // re-triggers the on-factory of its own twinflow (see runFactoryOnCreate/runFactoryOnUpdate guards),
    // so this is a true cascade bounded by cascadeDepth. Deletes are not supported in a cascade run
    // (they would require drafting) and fail the whole operation.
    private void cascadeApplyExtras(TwinSave inputTwin,
                                    FactoryResultUncommited result,
                                    TwinflowFactoryEntity twinflowFactory,
                                    TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (!result.getDeletes().isEmpty()) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT,
                    "Factory {} produced deletes during a cascade run on {}, but deletes are not supported in a cascade",
                    twinflowFactory.logNormal(), inputTwin.getTwinEntity().logNormal());
        }
        UUID inputTwinId = inputTwin.getTwinId();
        // the remaining cascade budget for the input: explicit (a cascade extra) or the configured max (a direct top-level twin)
        int budget = inputTwin.getCascadeDepth() != null ? inputTwin.getCascadeDepth() : maxCascadeDepth;
        int childDepth = budget - 1;
        if (KitUtils.isNotEmpty(result.getCreates())) {
            var extraTwinCreates = new TwinCreateStage(result.getCreates().size());
            for (TwinCreate extra : result.getCreates()) {
                if (extra.getTwinId().equals(inputTwinId)) {
                    continue; // the input twin itself is owned by the calling create/update flow
                }
                // after-operation factories are disabled for cascade extras so the cascade has a single
                // (synchronous, depth-bounded) channel and does not fan out via async TwinChangeTask jobs
                extra
                        .setCanTriggerAfterOperationFactory(false)
                        .setCascadeDepth(childDepth);
                extraTwinCreates.add(extra);
            }
            twinService.createTwins(extraTwinCreates, twinChangesCollector);
        }
        if (KitUtils.isNotEmpty(result.getUpdates())) {
            var extraTwinUpdates = new ArrayList<TwinUpdate>(result.getUpdates().size());
            for (TwinUpdate extra : result.getUpdates()) {
                if (extra.getTwinId().equals(inputTwinId)) {
                    continue;
                }
                extra
                        .setCanTriggerAfterOperationFactory(false)
                        .setCascadeDepth(childDepth);
                extraTwinUpdates.add(extra);
            }
            twinService.updateTwin(extraTwinUpdates, twinChangesCollector, false);
        }
    }

    public void runFactoryAfter(TwinSave twinSave, TwinChangesCollector twinChangesCollector, FactoryLauncher factoryLauncher) throws ServiceException {
        if (!twinSave.isCanTriggerAfterOperationFactory()) {
            log.info("{} is locked for {}", factoryLauncher, twinSave.getTwinEntity().logNormal());
            return;
        }
        TwinEntity twinEntity = detectTwinEntity(twinSave);
        twinflowService.loadTwinflow(twinEntity);
        loadFactories(twinEntity.getTwinflow());
        TwinflowFactoryEntity twinflowFactory = twinEntity.getTwinflow().getFactoriesKit().get(factoryLauncher);
        if (twinflowFactory == null) {
            return;
        }
        log.info("twin change task will be created for {} ", twinflowFactory.logNormal());
        twinChangesCollector.addPostponedChange(twinEntity.getId(), twinflowFactory.getTwinFactoryId(), twinflowFactory.getTwinFactoryLauncher());
    }

    private static TwinEntity detectTwinEntity(TwinSave twinSave) {
        TwinEntity twinEntity;
        if (twinSave instanceof TwinUpdate twinUpdate) {
            twinEntity = twinUpdate.getDbTwinEntity();
        } else
            twinEntity = twinSave.getTwinEntity();
        return twinEntity;
    }

    public List<TwinflowFactoryEntity> createTwinflowFactories(List<TwinflowFactoryEntity> twinflowFactoryEntity) throws ServiceException {
        return StreamSupport.stream(saveSafe(twinflowFactoryEntity).spliterator(), false).toList();
    }

    public List<TwinflowFactoryEntity> updateTwinflowFactory(Collection<TwinflowFactoryEntity> updateEntities) throws ServiceException {
        Kit<TwinflowFactoryEntity, UUID> updateKit = new Kit<>(updateEntities, TwinflowFactoryEntity::getId);
        Kit<TwinflowFactoryEntity, UUID> dbEntities = findEntitiesSafe(updateKit.getIdSet());
        ChangesHelper changesHelper = new ChangesHelper();
        ChangesHelperMulti<TwinflowFactoryEntity> changesHelperMulti = new ChangesHelperMulti<>();

        for (var dbEntity : dbEntities) {
            updateEntityFieldByValueIfNotNull(
                    updateKit.get(dbEntity.getId()).getTwinflowId(),
                    dbEntity,
                    TwinflowFactoryEntity::getTwinflowId,
                    TwinflowFactoryEntity::setTwinflowId,
                    TwinflowFactoryEntity.Fields.twinflowId,
                    changesHelper
            );

            updateEntityFieldByValueIfNotNull(
                    updateKit.get(dbEntity.getId()).getTwinFactoryId(),
                    dbEntity,
                    TwinflowFactoryEntity::getTwinFactoryId,
                    TwinflowFactoryEntity::setTwinFactoryId,
                    TwinflowFactoryEntity.Fields.twinFactoryId,
                    changesHelper
            );

            updateEntityFieldByValueIfNotNull(
                    updateKit.get(dbEntity.getId()).getTwinFactoryLauncher(),
                    dbEntity,
                    TwinflowFactoryEntity::getTwinFactoryLauncher,
                    TwinflowFactoryEntity::setTwinFactoryLauncher,
                    TwinflowFactoryEntity.Fields.twinFactoryLauncher,
                    changesHelper
            );
            changesHelperMulti.add(dbEntity, changesHelper);
        }

        return StreamSupport.stream(updateSafe(changesHelperMulti).spliterator(), false).toList();
    }

    public boolean existsNone(Collection<TwinflowFactoryEntity> entities) {
        return entities.stream()
                .noneMatch(entity -> twinflowFactoryRepository.existsByTwinflowIdAndTwinFactoryLauncher(entity.getTwinflowId(), entity.getTwinFactoryLauncher()));
    }
}
