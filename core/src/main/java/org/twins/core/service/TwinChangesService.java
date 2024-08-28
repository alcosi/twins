package org.twins.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.*;
import org.twins.core.domain.TwinChangesApplyResult;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.service.history.HistoryService;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TwinChangesService {
    final TwinRepository twinRepository;
    final TwinFieldSimpleRepository twinFieldSimpleRepository;
    final TwinFieldDataListRepository twinFieldDataListRepository;
    final TwinLinkRepository twinLinkRepository;
    final TwinFieldUserRepository twinFieldUserRepository;
    final TwinMarkerRepository twinMarkerRepository;
    final TwinTagRepository twinTagRepository;
    final EntitySmartService entitySmartService;
    final HistoryService historyService;

    public TwinChangesApplyResult applyChanges(TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinChangesApplyResult changesApplyResult = new TwinChangesApplyResult();
        if (!twinChangesCollector.hasChanges())
            return changesApplyResult; ;
        saveEntities(twinChangesCollector, TwinEntity.class, twinRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinFieldSimpleEntity.class, twinFieldSimpleRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinFieldDataListEntity.class, twinFieldDataListRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinFieldUserEntity.class, twinFieldUserRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinLinkEntity.class, twinLinkRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinMarkerEntity.class, twinMarkerRepository, changesApplyResult);
        saveEntities(twinChangesCollector, TwinTagEntity.class, twinTagRepository, changesApplyResult);
        if (!twinChangesCollector.getSaveEntityMap().isEmpty())
            for (Map.Entry<Class<?>, Map<Object, ChangesHelper>> classChanges : twinChangesCollector.getSaveEntityMap().entrySet()) {
                log.warn("Unsupported entity class[" + classChanges.getKey().getSimpleName() + "] for saving");
            }
        deleteEntities(twinChangesCollector, TwinEntity.class, twinRepository);
        deleteEntities(twinChangesCollector, TwinFieldDataListEntity.class, twinFieldDataListRepository);
        deleteEntities(twinChangesCollector, TwinFieldSimpleEntity.class, twinFieldSimpleRepository);
        deleteEntities(twinChangesCollector, TwinFieldUserEntity.class, twinFieldUserRepository);
        deleteEntities(twinChangesCollector, TwinLinkEntity.class, twinLinkRepository);
        deleteEntities(twinChangesCollector, TwinMarkerEntity.class, twinMarkerRepository);
        deleteEntities(twinChangesCollector, TwinTagEntity.class, twinTagRepository);
        if (!twinChangesCollector.getDeleteEntityMap().isEmpty())
            for (Map.Entry<Class<?>, Set<Object>> classChanges : twinChangesCollector.getDeleteEntityMap().entrySet()) {
                log.warn("Unsupported entity class[" + classChanges.getKey().getSimpleName() + "] for deletion");
            }
        invalidate(twinChangesCollector.getInvalidationMap());
        historyService.saveHistory(twinChangesCollector.getHistoryCollector());
        return changesApplyResult;
    }

    private void invalidate(Map<TwinEntity, Set<TwinChangesCollector.TwinInvalidate>> invalidationMap) {
        for (var entry : invalidationMap.entrySet()) {
            for (TwinChangesCollector.TwinInvalidate invalidation : entry.getValue()) {
                switch (invalidation) {
                    case tagsKit:
                        entry.getKey().setTwinTagKit(null);
                        break;
                    case markersKit:
                        entry.getKey().setTwinMarkerKit(null);
                        break;
                    case twinFieldSimpleKit:
                        entry.getKey().setTwinFieldSimpleKit(null);
                        break;
                    case twinFieldUserKit:
                        entry.getKey().setTwinFieldUserKit(null);
                        break;
                    case twinFieldDatalistKit:
                        entry.getKey().setTwinFieldDatalistKit(null);
                        break;
                    case twinLinks:
                        entry.getKey().setTwinLinks(null);
                        break;
                    case fieldValuesKit:
                        entry.getKey().setFieldValuesKit(null);
                        break;
                }
            }
        }
    }

    // in some cases we need to story history only, and all entities changes will be stored in other way (for best performance)
    public void saveHistoryOnly(TwinChangesCollector twinChangesCollector) throws ServiceException {
        historyService.saveHistory(twinChangesCollector.getHistoryCollector());
    }

    private <T> void saveEntities(TwinChangesCollector twinChangesCollector, Class<T> entityClass, CrudRepository<T, UUID> repository, TwinChangesApplyResult changesApplyResult) {
        Map<Object, ChangesHelper> entities = twinChangesCollector.getSaveEntityMap().get(entityClass);
        if (entities != null) {
            changesApplyResult.put(entityClass, entitySmartService.saveAllAndLogChanges((Map) entities, repository));
            twinChangesCollector.getSaveEntityMap().remove(entityClass);
        }
    }

    private <T> void deleteEntities(TwinChangesCollector twinChangesCollector, Class<T> entityClass, CrudRepository<T, UUID> repository) {
        Set<T> entities = (Set<T>) twinChangesCollector.getDeleteEntityMap().get(entityClass);
        if (entities != null) {
            entitySmartService.deleteAllEntitiesAndLog(entities, repository);
            twinChangesCollector.getDeleteEntityMap().remove(entityClass);
        }
    }
}
