package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dao.twinflow.TwinflowFactoryRepository;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.factory.*;
import org.twins.core.domain.twinoperation.TwinSave;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.service.factory.TwinFactoryService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class TwinflowFactoryService extends EntitySecureFindServiceImpl<TwinflowFactoryEntity> {
    private final TwinflowFactoryRepository repository;
    private final TwinflowService twinflowService;
    @Lazy
    private final TwinFactoryService twinFactoryService;

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
        if(entity.getTwinflowId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinflowId");
        if(entity.getTwinFactoryId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinFactoryId");
        if(entity.getTwinFactorylauncher() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty factoryLauncher");

        switch (entityValidateMode) {
            case beforeSave:
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
                twinflow.setFactoriesKit(new Kit<>(loaded.getGrouped(twinflow.getId()), TwinflowFactoryEntity::getTwinFactorylauncher));
            } else {
                twinflow.setFactoriesKit(Kit.EMPTY);
            }
        }
    }

    public void runFactoryOn(TwinSave twinSave, FactoryLauncher factoryLauncher) throws ServiceException {
        TwinEntity twinEntity = detectTwinEntity(twinSave);
        twinflowService.loadTwinflow(twinEntity);
        loadFactories(twinEntity.getTwinflow());
        TwinflowFactoryEntity twinflowFactory = twinEntity.getTwinflow().getFactoriesKit().get(factoryLauncher);
        runFactoryOn(twinSave, twinflowFactory);
    }

    public void runFactoryOn(TwinSave twinSave, TwinflowFactoryEntity twinflowFactory) throws ServiceException {
        if (twinflowFactory == null)
            return;
        FactoryContext factoryContext = new FactoryContext(twinflowFactory.getTwinFactorylauncher(), FactoryBranchId.root(twinflowFactory.getTwinFactoryId()));
        factoryContext.add(new FactoryItem().setOutput(twinSave).setFactoryContext(factoryContext));
        FactoryResultUncommited result = twinFactoryService.runFactoryAndCollectResult(twinflowFactory.getTwinFactoryId(), factoryContext);
        if (result.getUpdates().size() > 1 || !result.getCreates().isEmpty() || !result.getDeletes().isEmpty()) {
            log.warn("During [{}] operation, some extra twins where modified, but they won't be saved. " +
                    "Only current twin modification will make sense", twinflowFactory);
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
            log.info("No {} factory configured for {}", factoryLauncher, twinEntity.getTwinflow().logNormal()); //todo delete me in future
            return;
        }
        log.info("twin change task will be created for {} ", twinflowFactory.logNormal());
        twinChangesCollector.addPostponedChange(twinEntity.getId(), twinflowFactory.getTwinFactoryId(), twinflowFactory.getTwinFactorylauncher());
    }

    private static TwinEntity detectTwinEntity(TwinSave twinSave) {
        TwinEntity twinEntity;
        if (twinSave instanceof TwinUpdate twinUpdate) {
            twinEntity = twinUpdate.getDbTwinEntity();
        } else
            twinEntity = twinSave.getTwinEntity();
        return twinEntity;
    }
}
